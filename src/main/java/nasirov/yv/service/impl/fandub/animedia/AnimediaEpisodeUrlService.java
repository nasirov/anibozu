package nasirov.yv.service.impl.fandub.animedia;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.FanDubProps;
import nasirov.yv.fandub.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnimediaParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animedia.AnimediaFeignClient;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
public class AnimediaEpisodeUrlService extends BaseEpisodeUrlService {

	private final AnimediaFeignClient animediaFeignClient;

	private final AnimediaParserI animediaParser;

	public AnimediaEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, AnimediaFeignClient animediaFeignClient,
			AnimediaParserI animediaParser) {
		super(titlesService, fanDubProps);
		this.animediaFeignClient = animediaFeignClient;
		this.animediaParser = animediaParser;
	}

	@Override
	protected String buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		log.debug("Building url in runtime...");
		CommonTitle commonTitle = Collections.max(matchedTitles, Comparator.comparing(CommonTitle::getDataList));
		List<FandubEpisode> episodes = getEpisodes(commonTitle);
		return episodes.stream()
				.filter(x -> StringUtils.equals(nextEpisodeForWatch.toString(), x.getNumber()))
				.findFirst()
				.map(x -> fandubUrl + commonTitle.getUrl() + "/" + commonTitle.getDataList() + "/" + x.getId())
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		List<AnimediaEpisode> titlePage = animediaFeignClient.getTitleEpisodesByPlaylist(commonTitle.getId(), commonTitle.getDataList());
		return animediaParser.extractEpisodes(titlePage);
	}
}
