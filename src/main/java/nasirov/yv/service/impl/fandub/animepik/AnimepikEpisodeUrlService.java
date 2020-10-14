package nasirov.yv.service.impl.fandub.animepik;

import java.util.List;
import nasirov.yv.fandub.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animepik.AnimepikResourcesFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class AnimepikEpisodeUrlService extends BaseEpisodeUrlService {

	private final AnimepikResourcesFeignClient animepikResourcesFeignClient;

	private final AnimepikParserI animepikParser;

	public AnimepikEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, AnimepikResourcesFeignClient animepikResourcesFeignClient,
			AnimepikParserI animepikParser) {
		super(titlesService, fanDubProps);
		this.animepikResourcesFeignClient = animepikResourcesFeignClient;
		this.animepikParser = animepikParser;
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		List<AnimepikEpisode> animepikEpisodes = animepikResourcesFeignClient.getTitleEpisodes(commonTitle.getId());
		fillAnimepikEpisodesWithTitleUrl(animepikEpisodes, commonTitle.getUrl());
		return animepikParser.extractEpisodes(animepikEpisodes);
	}

	private void fillAnimepikEpisodesWithTitleUrl(List<AnimepikEpisode> animepikEpisodes, String titleUrl) {
		animepikEpisodes.forEach(x -> x.setTitleUrl(titleUrl));
	}
}
