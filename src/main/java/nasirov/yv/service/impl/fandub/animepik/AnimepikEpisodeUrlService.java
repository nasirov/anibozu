package nasirov.yv.service.impl.fandub.animepik;

import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.anime_pik.api.AnimepikEpisode;
import nasirov.yv.data.anime_pik.api.AnimepikTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnimepikResourcesFeignClient;
import nasirov.yv.parser.AnimepikParserI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@RequiredArgsConstructor
public class AnimepikEpisodeUrlService implements EpisodeUrlServiceI {

	private final TitlesServiceI<AnimepikTitle> animepikTitlesService;

	private final AnimepikResourcesFeignClient animepikResourcesFeignClient;

	private final UrlsNames urlsNames;

	private final AnimepikParserI animepikParser;

	@Override
	public String getEpisodeUrl(MalTitle watchingTitle) {
		String url = NOT_FOUND_ON_FANDUB_SITE_URL;
		AnimepikTitle matchedTitle = getMatchedTitle(watchingTitle);
		if (nonNull(matchedTitle)) {
			List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
			url = episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getAnimepikUrls()
					.getAnimepikUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
		}
		return url;
	}

	private AnimepikTitle getMatchedTitle(MalTitle watchingTitle) {
		return Optional.ofNullable(animepikTitlesService.getTitles()
				.get(watchingTitle.getId()))
				.orElseGet(Collections::emptyList)
				.stream()
				.findFirst()
				.orElse(null);
	}

	private List<Integer> extractAvailableEpisodes(AnimepikTitle matchedTitle) {
		return animepikResourcesFeignClient.getTitleEpisodes(matchedTitle.getId())
				.stream()
				.map(AnimepikEpisode::getName)
				.map(animepikParser::extractEpisodeNumber)
				.collect(Collectors.toList());
	}

}
