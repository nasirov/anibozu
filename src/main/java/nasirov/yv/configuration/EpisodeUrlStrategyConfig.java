package nasirov.yv.configuration;

import static nasirov.yv.data.constants.FanDubSource.ANIDUB;
import static nasirov.yv.data.constants.FanDubSource.ANILIBRIA;
import static nasirov.yv.data.constants.FanDubSource.ANIMEDIA;
import static nasirov.yv.data.constants.FanDubSource.ANIMEPIK;
import static nasirov.yv.data.constants.FanDubSource.JISEDAI;
import static nasirov.yv.data.constants.FanDubSource.NINEANIME;

import java.util.EnumMap;
import java.util.Map;
import nasirov.yv.data.constants.FanDubSource;
import nasirov.yv.data.fandub.anilibria.site.AnilibriaSiteTitle;
import nasirov.yv.data.fandub.anime_pik.api.AnimepikTitle;
import nasirov.yv.data.fandub.animedia.AnimediaTitle;
import nasirov.yv.data.fandub.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.service.AnidubEpisodeUrlServiceI;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.common.BaseEpisodeUrlService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class EpisodeUrlStrategyConfig {

	@Bean
	public Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy(BaseEpisodeUrlService<AnimediaTitle> animediaEpisodeUrlService,
			NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService, AnidubEpisodeUrlServiceI anidubEpisodeUrlService,
			BaseEpisodeUrlService<JisedaiSiteTitle> jisedaiEpisodeUrlService, BaseEpisodeUrlService<AnimepikTitle> animepikEpisodeUrlService,
			BaseEpisodeUrlService<AnilibriaSiteTitle> anilibriaEpisodeUrlService) {
		EnumMap<FanDubSource, EpisodeUrlServiceI> map = new EnumMap<>(FanDubSource.class);
		map.put(ANIMEDIA, animediaEpisodeUrlService);
		map.put(NINEANIME, nineAnimeEpisodeUrlService);
		map.put(ANIDUB, anidubEpisodeUrlService);
		map.put(JISEDAI, jisedaiEpisodeUrlService);
		map.put(ANIMEPIK, animepikEpisodeUrlService);
		map.put(ANILIBRIA, anilibriaEpisodeUrlService);
		return map;
	}
}
