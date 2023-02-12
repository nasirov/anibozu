package nasirov.yv.ab.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.controller.ExceptionHandlers;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.dto.fe.Title;
import nasirov.yv.ab.dto.fe.Title.TitleBuilder;
import nasirov.yv.ab.dto.fe.TitleType;
import nasirov.yv.ab.dto.mal.MalUserInfo;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.CommonTitlesServiceI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.ProcessServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService implements ProcessServiceI {

	private static final ProcessResult FALLBACK_VALUE = new ProcessResult(ExceptionHandlers.GENERIC_ERROR_MESSAGE);

	private final MalServiceI malService;

	private final CommonTitlesServiceI commonTitlesService;

	private final AppProps appProps;

	private final StarterCommonProperties starterCommonProperties;

	private Map<FandubSource, String> fandubMap;

	@PostConstruct
	public void init() {
		this.fandubMap = appProps.getEnabledFandubSources()
				.stream()
				.collect(Collectors.toMap(Function.identity(), FandubSource::getCanonicalName));
	}

	@Override
	public Mono<ProcessResult> process(String username) {
		return Mono.just(username)
				.flatMap(x -> malService.getMalUserInfo(username))
				.flatMap(this::buildProcessResult)
				.defaultIfEmpty(FALLBACK_VALUE)
				.onErrorReturn(FALLBACK_VALUE)
				.doOnSubscribe(x -> log.info("Processing [{}]...", username))
				.doOnSuccess(x -> log.info("Processed [{}]{}", username,
						StringUtils.isNotBlank(x.getErrorMessage()) ? " with Error Message [" + x.getErrorMessage() + "]" : ""));
	}

	private Mono<ProcessResult> buildProcessResult(MalUserInfo malUserInfo) {
		Mono<ProcessResult> result;
		String malServiceErrorMessage = malUserInfo.getErrorMessage();
		boolean blankMalServiceErrorMessage = StringUtils.isBlank(malServiceErrorMessage);
		List<MalTitle> malTitles = malUserInfo.getMalTitles();
		if (blankMalServiceErrorMessage && CollectionUtils.isNotEmpty(malTitles)) {
			Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream()
					.collect(Collectors.toMap(MalTitle::getId, Function.identity()));
			result = commonTitlesService.getCommonTitles(appProps.getEnabledFandubSources(), malTitles)
					.map(x -> x.entrySet()
							.stream()
							.map(e -> buildTitle(malIdToMalTitle.get(e.getKey()), e.getValue()))
							.collect(Collectors.toList()))
					.map(x -> new ProcessResult(x, fandubMap));
		} else {
			result = Mono.just(blankMalServiceErrorMessage ? FALLBACK_VALUE : new ProcessResult(malServiceErrorMessage));
		}
		return result;
	}

	private Title buildTitle(MalTitle watchingTitle, Map<FandubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = watchingTitle.getNextEpisodeForWatch();
		TitleBuilder titleBuilder = Title.builder()
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
				titleBuilder.fandubToEpisodeName(fandubSource, episodeNameToUrl.getKey());
				titleBuilder.fandubToUrl(fandubSource, episodeNameToUrl.getValue());
				titleType = TitleType.AVAILABLE;
			}
		}
		return titleBuilder.type(titleType).build();
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
}
