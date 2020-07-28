package nasirov.yv.service.impl.fandub.animepik;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.List;
import java.util.stream.Collectors;
import nasirov.yv.data.fandub.anime_pik.AnimepikTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.fandub.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animepik.AnimepikResourcesFeignClient;
import nasirov.yv.parser.AnimepikParserI;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class AnimepikEpisodeUrlService extends BaseEpisodeUrlService<AnimepikTitle> {

	private final AnimepikResourcesFeignClient animepikResourcesFeignClient;

	private final UrlsNames urlsNames;

	private final AnimepikParserI animepikParser;

	public AnimepikEpisodeUrlService(TitlesServiceI<AnimepikTitle> animepikTitlesService, AnimepikResourcesFeignClient animepikResourcesFeignClient,
			UrlsNames urlsNames, AnimepikParserI animepikParser) {
		super(animepikTitlesService);
		this.animepikResourcesFeignClient = animepikResourcesFeignClient;
		this.urlsNames = urlsNames;
		this.animepikParser = animepikParser;
	}

	@Override
	protected String buildUrl(MalTitle watchingTitle, List<AnimepikTitle> matchedTitles) {
		AnimepikTitle matchedTitle = matchedTitles.get(0);
		List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
		return episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getAnimepikUrls()
				.getAnimepikUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
	}

	private List<Integer> extractAvailableEpisodes(AnimepikTitle matchedTitle) {
		return animepikResourcesFeignClient.getTitleEpisodes(matchedTitle.getId())
				.stream()
				.map(AnimepikEpisode::getName)
				.map(animepikParser::extractEpisodeNumber)
				.collect(Collectors.toList());
	}

}
