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
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.dto.fe.Title;
import nasirov.yv.ab.dto.fe.Title.TitleBuilder;
import nasirov.yv.ab.dto.fe.TitleType;
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

	private Map<FandubSource, String> fandubMap;

	@PostConstruct
	public void init() {
		this.fandubMap = appProps.getEnabledFandubSources()
				.stream()
				.collect(Collectors.toMap(Function.identity(), FandubSource::getCanonicalName));
	}

	@Override
	public Mono<ProcessResult> process(String username) {
		return malService.getMalTitles(username)
				.flatMap(this::buildProcessResult)
				.doOnSubscribe(x -> log.info("Processing {}", username))
				.doOnSuccess(x -> log.info("Done {}", username));
	}

	private Mono<ProcessResult> buildProcessResult(List<MalTitle> malTitles) {
		Map<Integer, MalTitle> malIdToMalTitle = malTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, Function.identity()));
		return commonTitlesService.getCommonTitles(appProps.getEnabledFandubSources(), malTitles)
				.map(x -> new ProcessResult(
						x.entrySet().stream().map(e -> buildTitle(malIdToMalTitle.get(e.getKey()), e.getValue())).toList(), fandubMap));
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
