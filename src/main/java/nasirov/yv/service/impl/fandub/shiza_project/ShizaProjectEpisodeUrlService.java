package nasirov.yv.service.impl.fandub.shiza_project;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;

import com.google.common.collect.Iterables;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.ShizaProjectParserI;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.shiza_project.ShizaProjectFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
public class ShizaProjectEpisodeUrlService extends BaseEpisodeUrlService {

	private final ShizaProjectFeignClient shizaProjectFeignClient;

	private final ShizaProjectParserI shizaProjectParser;

	public ShizaProjectEpisodeUrlService(TitlesServiceI titlesService, FanDubProps fanDubProps, ShizaProjectFeignClient shizaProjectFeignClient,
			ShizaProjectParserI shizaProjectParser) {
		super(titlesService, fanDubProps);
		this.shizaProjectFeignClient = shizaProjectFeignClient;
		this.shizaProjectParser = shizaProjectParser;
	}

	@Override
	protected String buildUrl(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		return matchedTitles.stream()
				.map(CommonTitle::getEpisodes)
				.flatMap(List::stream)
				.filter(x -> nextEpisodeForWatch.equals(x.getMalEpisodeId()))
				.findFirst()
				.map(CommonEpisode::getUrl)
				.orElseGet(() -> buildUrlAlt(nextEpisodeForWatch, matchedTitles, fandubUrl));
	}

	@Override
	protected String buildUrlInRuntime(Integer nextEpisodeForWatch, List<CommonTitle> matchedTitles, String fandubUrl) {
		log.debug("Building url in runtime...");
		CommonTitle commonTitle = Iterables.get(matchedTitles, 0);
		List<FandubEpisode> episodes = getEpisodes(commonTitle);
		return episodes.stream()
				.filter(x -> nextEpisodeForWatch.equals(x.getId()))
				.findFirst()
				.map(FandubEpisode::getUrl)
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}

	@Override
	protected List<FandubEpisode> getEpisodes(CommonTitle commonTitle) {
		String titlePage = shizaProjectFeignClient.getTitlePage(commonTitle.getUrl());
		Document document = Jsoup.parse(titlePage);
		return shizaProjectParser.extractEpisodes(document);
	}
}
