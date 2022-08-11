package nasirov.yv.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.front.InputDto;
import nasirov.yv.data.front.ResultDto;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.data.front.TitleDto.TitleDtoBuilder;
import nasirov.yv.data.front.TitleType;
import nasirov.yv.data.properties.CacheProps;
import nasirov.yv.data.properties.FandubSupportProps;
import nasirov.yv.service.FandubTitlesServiceI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ResultProcessingServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultProcessingService implements ResultProcessingServiceI {

	private static final ResultDto FALLBACK_VALUE = new ResultDto(BaseConstants.GENERIC_ERROR_MESSAGE);

	private final MalServiceI malService;

	private final FandubTitlesServiceI fandubTitlesService;

	private final CacheManager cacheManager;

	private final CacheProps cacheProps;

	private final StarterCommonProperties starterCommonProperties;

	private final FandubSupportProps fandubSupportProps;

	@Override
	public Mono<ResultDto> getResult(InputDto inputDto) {
		return getCache().map(x -> x.get(inputDto.getUsername(), ResultDto.class))
				.map(Mono::just)
				.orElse(Mono.just(inputDto)
						.flatMap(malService::getUserWatchingTitles)
						.flatMap(this::buildResult)
						.defaultIfEmpty(FALLBACK_VALUE)
						.onErrorReturn(FALLBACK_VALUE)
						.doOnSuccess(x -> cacheResult(inputDto, x)));
	}

	private Mono<ResultDto> buildResult(MalServiceResponseDto malServiceResponseDto) {
		Mono<ResultDto> result;
		String malServiceErrorMessage = malServiceResponseDto.getErrorMessage();
		if (StringUtils.isBlank(malServiceErrorMessage) && CollectionUtils.isNotEmpty(malServiceResponseDto.getMalTitles())) {
			result = fandubTitlesService.getCommonTitles(getEnabledFandubSources(), malServiceResponseDto.getMalTitles())
					.map(y -> buildResult(y, malServiceResponseDto.getMalTitles()));
		} else {
			result = Mono.just(
					StringUtils.isBlank(malServiceErrorMessage) ? FALLBACK_VALUE : new ResultDto(malServiceErrorMessage));
		}
		return result;
	}

	private Set<FandubSource> getEnabledFandubSources() {
		return fandubSupportProps.getEnabled();
	}

	private ResultDto buildResult(Map<Integer, Map<FandubSource, List<CommonTitle>>> malIdToMatchedCommonTitlesByFandubSource,
			List<MalTitle> malTitles) {
		ResultDto result = new ResultDto(getEnabledFandubSources());
		Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, Function.identity()));
		for (Entry<Integer, Map<FandubSource, List<CommonTitle>>> entry : malIdToMatchedCommonTitlesByFandubSource.entrySet()) {
			TitleDto titleDto = buildTitle(malIdToMalTitle.get(entry.getKey()), entry.getValue());
			result.getTitles().add(titleDto);
		}
		return result;
	}

	private TitleDto buildTitle(MalTitle watchingTitle, Map<FandubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		TitleDtoBuilder titleDtoBuilder = TitleDto.builder()
				.nameOnMal(watchingTitle.getName())
				.episodeNumberOnMal(nextEpisodeForWatch.toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl());
		TitleType titleType = TitleType.NOT_AVAILABLE;
		for (Entry<FandubSource, List<CommonTitle>> entry : commonTitlesByFandubSource.entrySet()) {
			FandubSource fandubSource = entry.getKey();
			Optional<Pair<String, String>> result = Optional.of(entry.getValue())
					.filter(CollectionUtils::isNotEmpty)
					.flatMap(x -> buildNameAndUrlPair(nextEpisodeForWatch, x,
							starterCommonProperties.getFandub().getUrls().get(fandubSource)));
			if (result.isPresent()) {
				Pair<String, String> episodeNameToUrl = result.get();
				titleDtoBuilder.fandubToEpisodeName(fandubSource, episodeNameToUrl.getKey());
				titleDtoBuilder.fandubToUrl(fandubSource, episodeNameToUrl.getValue());
				titleType = TitleType.AVAILABLE;
			}
		}
		return titleDtoBuilder.type(titleType).build();
	}

	private Optional<Pair<String, String>> buildNameAndUrlPair(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles,
			String fandubUrl) {
		return matchedTitles.stream()
				.flatMap(x -> x.getMalIdToEpisodes().values().stream().flatMap(List::stream))
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()));
	}

	private void cacheResult(InputDto inputDto, ResultDto result) {
		String username = inputDto.getUsername();
		String errorMessage = result.getErrorMessage();
		log.info("Titles [{}], Error Message [{}] for [{}].", result.getTitles().size(), errorMessage, username);
		if (!StringUtils.equals(BaseConstants.GENERIC_ERROR_MESSAGE, errorMessage)) {
			getCache().ifPresent(x -> x.put(username, result));
		}
	}

	private Optional<Cache> getCache() {
		return Optional.ofNullable(cacheManager.getCache(cacheProps.getResult().getName()));
	}
}
