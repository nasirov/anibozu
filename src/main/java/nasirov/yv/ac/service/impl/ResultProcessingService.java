package nasirov.yv.ac.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ac.dto.fe.InputDto;
import nasirov.yv.ac.dto.fe.ResultDto;
import nasirov.yv.ac.dto.fe.TitleDto;
import nasirov.yv.ac.dto.fe.TitleDto.TitleDtoBuilder;
import nasirov.yv.ac.dto.fe.TitleType;
import nasirov.yv.ac.dto.mal.MalUserInfo;
import nasirov.yv.ac.properties.AppProps;
import nasirov.yv.ac.service.CommonTitlesServiceI;
import nasirov.yv.ac.service.MalServiceI;
import nasirov.yv.ac.service.ResultProcessingServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import nasirov.yv.starter.common.service.WrappedObjectMapperI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultProcessingService implements ResultProcessingServiceI {

	public static final String GENERIC_ERROR_MESSAGE = "Sorry, something went wrong.";

	private static final ResultDto FALLBACK_VALUE = new ResultDto(GENERIC_ERROR_MESSAGE);

	private final MalServiceI malService;

	private final CommonTitlesServiceI commonTitlesService;

	private final AppProps appProps;

	private final StarterCommonProperties starterCommonProperties;

	private final WrappedObjectMapperI wrappedObjectMapper;

	private String fandubMapJson;

	@PostConstruct
	public void init() {
		this.fandubMapJson = toEscapedJson(appProps.getEnabledFandubSources()
				.stream()
				.collect(Collectors.toMap(FandubSource::name, FandubSource::getCanonicalName)));
	}

	@Override
	public Mono<ResultDto> getResult(InputDto inputDto) {
		return Mono.just(inputDto)
				.flatMap(x -> malService.getMalUserInfo(x.getUsername()))
				.flatMap(this::buildResult)
				.defaultIfEmpty(FALLBACK_VALUE)
				.onErrorReturn(FALLBACK_VALUE)
				.doOnSubscribe(x -> log.info("Processing [{}]...", inputDto.getUsername()))
				.doOnSuccess(x -> log.info("Processed [{}]{}", inputDto.getUsername(),
						StringUtils.isNotBlank(x.getErrorMessage()) ? " with Error Message [" + x.getErrorMessage() + "]" : ""));
	}

	private Mono<ResultDto> buildResult(MalUserInfo malUserInfo) {
		Mono<ResultDto> result;
		String malServiceErrorMessage = malUserInfo.getErrorMessage();
		boolean blankMalServiceErrorMessage = StringUtils.isBlank(malServiceErrorMessage);
		List<MalTitle> malTitles = malUserInfo.getMalTitles();
		if (blankMalServiceErrorMessage && CollectionUtils.isNotEmpty(malTitles)) {
			result = commonTitlesService.getCommonTitles(appProps.getEnabledFandubSources(), malTitles)
					.map(x -> buildResult(x, malTitles));
		} else {
			result = Mono.just(blankMalServiceErrorMessage ? FALLBACK_VALUE : new ResultDto(malServiceErrorMessage));
		}
		return result;
	}

	private ResultDto buildResult(Map<Integer, Map<FandubSource, List<CommonTitle>>> malIdToMatchedCommonTitlesByFandubSource,
			List<MalTitle> malTitles) {
		Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, Function.identity()));
		List<TitleDto> titleDtos = new ArrayList<>();
		for (Entry<Integer, Map<FandubSource, List<CommonTitle>>> entry : malIdToMatchedCommonTitlesByFandubSource.entrySet()) {
			TitleDto titleDto = buildTitle(malIdToMalTitle.get(entry.getKey()), entry.getValue());
			titleDtos.add(titleDto);
		}
		return new ResultDto(toEscapedJson(titleDtos), fandubMapJson);
	}

	private TitleDto buildTitle(MalTitle watchingTitle, Map<FandubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = watchingTitle.getNextEpisodeForWatch();
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
					.flatMap(x -> buildNameAndUrlPair(x, fandubSource, nextEpisodeForWatch));
			if (result.isPresent()) {
				Pair<String, String> episodeNameToUrl = result.get();
				titleDtoBuilder.fandubToEpisodeName(fandubSource, episodeNameToUrl.getKey());
				titleDtoBuilder.fandubToUrl(fandubSource, episodeNameToUrl.getValue());
				titleType = TitleType.AVAILABLE;
			}
		}
		return titleDtoBuilder.type(titleType).build();
	}

	private Optional<Pair<String, String>> buildNameAndUrlPair(List<CommonTitle> matchedTitles, FandubSource fandubSource,
			Integer nextEpisodeForWatch) {
		String fandubUrl = starterCommonProperties.getFandub().getUrls().get(fandubSource);
		boolean ignoreNextEpisodeForWatch = appProps.getIgnoreNextEpisodeForWatch().contains(fandubSource);
		return matchedTitles.stream()
				.flatMap(x -> x.getMalIdToEpisodes().values().stream().flatMap(List::stream))
				.filter(x -> ignoreNextEpisodeForWatch || nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()));
	}

	private String toEscapedJson(Object value) {
		return HtmlUtils.htmlEscape(wrappedObjectMapper.toJson(value), StandardCharsets.UTF_8.name());
	}
}
