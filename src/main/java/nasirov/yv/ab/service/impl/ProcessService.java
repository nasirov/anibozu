package nasirov.yv.ab.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.FandubInfo;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.dto.fe.Title;
import nasirov.yv.ab.dto.fe.Title.TitleBuilder;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.CommonTitlesServiceI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.ProcessServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import org.apache.commons.collections4.CollectionUtils;
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

	private final MalServiceI malService;

	private final CommonTitlesServiceI commonTitlesService;

	private final AppProps appProps;

	private final StarterCommonProperties starterCommonProperties;

	@Override
	public Mono<ProcessResult> process(String username) {
		return malService.getMalTitles(username)
				.flatMap(this::buildProcessResult)
				.doOnSubscribe(x -> log.info("Processing {}", username))
				.doOnSuccess(x -> log.info("Done {}", username));
	}

	private Mono<ProcessResult> buildProcessResult(List<MalTitle> malTitles) {
		Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream().collect(Collectors.toMap(MalTitle::getId, Function.identity()));
		return commonTitlesService.getCommonTitles(appProps.getEnabledFandubSources(), malTitles)
				.map(x -> new ProcessResult(x.entrySet().stream().map(e -> buildTitle(malIdToMalTitle.get(e.getKey()), e.getValue())).toList()));
	}

	private Title buildTitle(MalTitle watchingTitle, Map<FandubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = watchingTitle.getNextEpisodeForWatch();
		TitleBuilder titleBuilder = Title.builder()
				.name(watchingTitle.getName())
				.nextEpisodeNumber(nextEpisodeForWatch.toString())
				.posterUrl(watchingTitle.getPosterUrl())
				.malUrl(watchingTitle.getAnimeUrl());
		commonTitlesByFandubSource.forEach((fandubSource, titles) -> Optional.of(titles)
				.filter(CollectionUtils::isNotEmpty)
				.flatMap(x -> buildNameAndUrlPair(x, fandubSource, nextEpisodeForWatch))
				.ifPresent(x -> titleBuilder.fandubInfoList(FandubInfo.builder()
						.fandubSource(fandubSource)
						.fandubSourceCanonicalName(fandubSource.getCanonicalName())
						.episodeUrl(x.getValue())
						.episodeName(x.getKey())
						.build())));
		return titleBuilder.build();
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
