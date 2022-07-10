package nasirov.yv.service.impl;

import static nasirov.yv.data.constants.BaseConstants.NOT_AVAILABLE_EPISODE_NAME_AND_URL;
import static nasirov.yv.data.constants.BaseConstants.TITLE_NOT_FOUND_EPISODE_NAME_AND_URL;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.TitleDto;
import nasirov.yv.data.front.TitleDto.TitleDtoBuilder;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.TitleServiceI;
import nasirov.yv.util.MalUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TitleService implements TitleServiceI {

	private final FanDubProps fanDubProps;

	@Override
	public TitleDto buildTitle(MalTitle watchingTitle, Map<FanDubSource, List<CommonTitle>> commonTitlesByFandubSource) {
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(watchingTitle);
		TitleDtoBuilder titleDtoBuilder = TitleDto.builder()
				.animeName(watchingTitle.getName())
				.malEpisodeNumber(nextEpisodeForWatch.toString())
				.posterUrlOnMal(watchingTitle.getPosterUrl())
				.animeUrlOnMal(watchingTitle.getAnimeUrl());
		commonTitlesByFandubSource.entrySet().forEach(x -> enrichWithEpisodeNameAndUrl(titleDtoBuilder, x,
				nextEpisodeForWatch));
		return titleDtoBuilder.build();
	}

	private void enrichWithEpisodeNameAndUrl(TitleDtoBuilder titleDtoBuilder, Entry<FanDubSource, List<CommonTitle>> entry,
			Integer nextEpisodeForWatch) {
		FanDubSource fanDubSource = entry.getKey();
		Pair<String, String> result = Optional.of(entry.getValue())
				.filter(CollectionUtils::isNotEmpty)
				.map(x -> buildNameAndUrlPair(nextEpisodeForWatch, x, fanDubProps.getUrls().get(fanDubSource)))
				.orElse(TITLE_NOT_FOUND_EPISODE_NAME_AND_URL);
		titleDtoBuilder.fanDubUrl(fanDubSource, result.getValue());
		titleDtoBuilder.fanDubEpisodeName(fanDubSource, result.getKey());
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
}
