package nasirov.yv.ab.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.FandubInfo;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.dto.fe.Title;
import nasirov.yv.ab.dto.fe.Title.TitleBuilder;
import nasirov.yv.ab.dto.internal.GithubCacheKey;
import nasirov.yv.ab.properties.AppProps;
import nasirov.yv.ab.service.CommonTitlesServiceI;
import nasirov.yv.ab.service.MalServiceI;
import nasirov.yv.ab.service.ProcessServiceI;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonEpisode;
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
				.flatMap(x -> commonTitlesService.getCommonEpisodesMappedByKey().map(m -> buildProcessResult(m, x)))
				.doOnSubscribe(x -> log.info("Processing [{}]", username))
				.doOnSuccess(x -> log.info("Done [{}]", username));
	}

	private ProcessResult buildProcessResult(Map<GithubCacheKey, List<CommonEpisode>> keyToCommonEpisodes, List<MalTitle> malTitles) {
		List<Title> titles = new ArrayList<>();
		for (MalTitle malTitle : malTitles) {
			Integer malId = malTitle.getId();
			Integer nextEpisodeForWatch = malTitle.getNextEpisodeForWatch();
			TitleBuilder titleBuilder = Title.builder()
					.name(malTitle.getName())
					.animeNumEpisodes(malTitle.getAnimeNumEpisodes().toString())
					.nextEpisodeNumber(nextEpisodeForWatch.toString())
					.posterUrl(malTitle.getPosterUrl())
					.malUrl(malTitle.getAnimeUrl());
			for (FandubSource fandubSource : appProps.getEnabledFandubSources()) {
				GithubCacheKey key = new GithubCacheKey(fandubSource, malId);
				Optional.ofNullable(keyToCommonEpisodes.get(key))
						.filter(CollectionUtils::isNotEmpty)
						.flatMap(x -> buildNameAndUrlPair(x, fandubSource, nextEpisodeForWatch))
						.ifPresent(x -> titleBuilder.fandubInfoList(FandubInfo.builder()
								.fandubSource(fandubSource)
								.fandubSourceCanonicalName(fandubSource.getCanonicalName())
								.episodeUrl(x.getValue())
								.episodeName(x.getKey())
								.build()));
			}
			titles.add(titleBuilder.build());
		}
		return new ProcessResult(titles);
	}

	private Optional<Pair<String, String>> buildNameAndUrlPair(List<CommonEpisode> commonEpisodesByMalId, FandubSource fandubSource,
			Integer nextEpisodeForWatch) {
		String fandubUrl = starterCommonProperties.getFandub().getUrls().get(fandubSource);
		boolean ignoreNextEpisodeForWatch = appProps.getIgnoreNextEpisodeForWatch().contains(fandubSource);
		return commonEpisodesByMalId.stream()
				.filter(x -> ignoreNextEpisodeForWatch || nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(x -> Pair.of(x.getName(), fandubUrl + x.getUrl()));
	}
}
