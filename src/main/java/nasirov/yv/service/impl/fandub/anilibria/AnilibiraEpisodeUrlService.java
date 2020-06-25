package nasirov.yv.service.impl.fandub.anilibria;

import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.util.MalUtils.getNextEpisodeForWatch;

import java.util.List;
import nasirov.yv.data.fandub.anilibria.AnilibriaTitle;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.fandub.anilibria.AnilibriaFeignClient;
import nasirov.yv.parser.AnilibriaParserI;
import nasirov.yv.service.TitlesServiceI;
import nasirov.yv.service.impl.fandub.BaseEpisodeUrlService;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class AnilibiraEpisodeUrlService extends BaseEpisodeUrlService<AnilibriaTitle> {

	private final AnilibriaFeignClient anilibriaFeignClient;

	private final UrlsNames urlsNames;

	private final AnilibriaParserI anilibriaParser;

	public AnilibiraEpisodeUrlService(TitlesServiceI<AnilibriaTitle> titlesService, AnilibriaFeignClient anilibriaFeignClient,
			UrlsNames urlsNames, AnilibriaParserI anilibriaParser) {
		super(titlesService);
		this.anilibriaFeignClient = anilibriaFeignClient;
		this.urlsNames = urlsNames;
		this.anilibriaParser = anilibriaParser;
	}

	@Override
	protected String buildUrl(MalTitle watchingTitle, List<AnilibriaTitle> matchedTitles) {
		AnilibriaTitle matchedTitle = matchedTitles.get(0);
		List<Integer> episodes = extractAvailableEpisodes(matchedTitle);
		return episodes.contains(getNextEpisodeForWatch(watchingTitle)) ? urlsNames.getAnilibriaUrls()
				.getAnilibriaUrl() + matchedTitle.getUrl() : FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
	}

	private List<Integer> extractAvailableEpisodes(AnilibriaTitle matchedTitle) {
		String titlePage = anilibriaFeignClient.getTitlePage(matchedTitle.getUrl());
		return anilibriaParser.extractEpisodes(titlePage);
	}
}
