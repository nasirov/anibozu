package nasirov.yv.service.impl.fandub.animepik;

import java.util.List;
import nasirov.yv.data.properties.AuthProps;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimepikParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animepik.AnimepikResourcesFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub_titles_service.FandubTitlesServiceFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Service
public class AnimepikEpisodeUrlService extends BaseEpisodeUrlService {

	private final AnimepikResourcesFeignClient animepikResourcesFeignClient;

	private final AnimepikParserI animepikParser;

	public AnimepikEpisodeUrlService(FanDubProps fanDubProps, FandubTitlesServiceFeignClient fandubTitlesServiceFeignClient, AuthProps authProps,
			CommonProps commonProps,
			AnimepikResourcesFeignClient animepikResourcesFeignClient,
			AnimepikParserI animepikParser) {
		super(fanDubProps, fandubTitlesServiceFeignClient, authProps, commonProps);
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
