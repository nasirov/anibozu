package nasirov.yv.service.impl;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_URL;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
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
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FandubProps;
import nasirov.yv.service.FandubTitlesServiceI;
import nasirov.yv.service.MalServiceI;
import nasirov.yv.service.ResultProcessingServiceI;
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

	private final FandubProps fanDubProps;

	@Override
	public Mono<ResultDto> getResult(InputDto inputDto) {
		String cacheKey = buildCacheKey(inputDto);
		return getCache().map(x -> x.get(cacheKey, ResultDto.class))
				.map(Mono::just)
				.orElse(Mono.just(inputDto)
						.flatMap(malService::getUserWatchingTitles)
						.flatMap(x -> buildResult(inputDto, x))
						.defaultIfEmpty(FALLBACK_VALUE)
						.onErrorReturn(FALLBACK_VALUE)
						.doOnSuccess(x -> cacheResult(inputDto, x, cacheKey)));
	}

	private Mono<ResultDto> buildResult(InputDto inputDto, MalServiceResponseDto malServiceResponseDto) {
		Mono<ResultDto> result;
		String malServiceErrorMessage = malServiceResponseDto.getErrorMessage();
		if (StringUtils.isBlank(malServiceErrorMessage)) {
			result = fandubTitlesService.getCommonTitles(inputDto.getFandubSources(), malServiceResponseDto.getMalTitles())
					.map(y -> buildResult(y, malServiceResponseDto.getMalTitles()));
		} else {
			result = Mono.just(new ResultDto(malServiceErrorMessage));
		}
		return result;
	}

	private ResultDto buildResult(Map<Integer, Map<FandubSource, List<CommonTitle>>> malIdToMatchedCommonTitlesByFandubSource,
			List<MalTitle> malTitles) {
		ResultDto result = new ResultDto();
		Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, Function.identity()));
		for (Entry<Integer, Map<FandubSource, List<CommonTitle>>> entry : malIdToMatchedCommonTitlesByFandubSource.entrySet()) {
			TitleDto titleDto = buildTitle(malIdToMalTitle.get(entry.getKey()), entry.getValue());
			switch (titleDto.getType()) {
				case AVAILABLE:
					result.getAvailableTitles().add(titleDto);
					break;
				case NOT_AVAILABLE:
					result.getNotAvailableTitles().add(titleDto);
					break;
				default:
					result.getNotFoundTitles().add(titleDto);
			}
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
		TitleType titleType = TitleType.NOT_FOUND;
		for (Entry<FandubSource, List<CommonTitle>> entry : commonTitlesByFandubSource.entrySet()) {
			FandubSource fandubSource = entry.getKey();
			Pair<String, String> result = Optional.of(entry.getValue())
					.filter(CollectionUtils::isNotEmpty)
					.map(x -> buildNameAndUrlPair(nextEpisodeForWatch, x, fanDubProps.getUrls().get(fandubSource)))
					.orElse(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL);
			String url = result.getValue();
			titleDtoBuilder.fandubToUrl(fandubSource, url);
			titleDtoBuilder.fandubToEpisodeName(fandubSource, result.getKey());
			titleType = determineTitleType(titleType, url);
		}
		return titleDtoBuilder.type(titleType).build();
	}

	private TitleType determineTitleType(TitleType bestCurrentType, String url) {
		if (isAvailable(url)) {
			bestCurrentType = TitleType.AVAILABLE;
		} else if (TitleType.AVAILABLE != bestCurrentType && isNotAvailable(url)) {
			bestCurrentType = TitleType.NOT_AVAILABLE;
		}
		return bestCurrentType;
	}

	private Pair<String, String> buildNameAndUrlPair(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles,
			String fandubUrl) {
		return matchedTitles.stream()
				.map(CommonTitle::getEpisodes)
				.flatMap(List::stream)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()))
				.orElse(NOT_AVAILABLE_EPISODE_NAME_AND_URL);
	}

	private boolean isAvailable(String url) {
		return !NOT_AVAILABLE_EPISODE_URL.equals(url) && !TITLE_NOT_FOUND_EPISODE_URL.equals(url);
	}

	private boolean isNotAvailable(String url) {
		return NOT_AVAILABLE_EPISODE_URL.equals(url);
	}

	private void cacheResult(InputDto inputDto, ResultDto result, String cacheKey) {
		log.info("Available [{}], Not Available [{}], Not Found [{}], Error Message [{}] for [{}] with {}.",
				result.getAvailableTitles().size(), result.getNotAvailableTitles().size(), result.getNotFoundTitles().size(),
				result.getErrorMessage(), inputDto.getUsername(), inputDto.getFandubSources());
		if (!StringUtils.equals(BaseConstants.GENERIC_ERROR_MESSAGE, result.getErrorMessage())) {
			getCache().ifPresent(x -> x.put(cacheKey, result));
		}
	}

	private String buildCacheKey(InputDto inputDto) {
		StringJoiner stringJoiner = new StringJoiner(",", inputDto.getUsername() + ":", "");
		inputDto.getFandubSources().forEach(x -> stringJoiner.add(x.name()));
		return stringJoiner.toString();
	}

	private Optional<Cache> getCache() {
		return Optional.ofNullable(cacheManager.getCache(cacheProps.getResult().getName()));
	}
}
