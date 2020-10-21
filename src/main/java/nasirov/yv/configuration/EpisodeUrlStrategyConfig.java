package nasirov.yv.configuration;


import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JISEDAI;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JUTSU;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SOVETROMANTICA;

import java.util.EnumMap;
import java.util.Map;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.fandub.anidub.AnidubEpisodeUrlService;
import nasirov.yv.service.impl.fandub.anilibria.AnilibriaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animedia.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.animepik.AnimepikEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jisedai.JisedaiEpisodeUrlService;
import nasirov.yv.service.impl.fandub.jutsu.JutsuEpisodeUrlService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import nasirov.yv.service.impl.fandub.sovet_romantica.SovetRomanticaEpisodeUrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class EpisodeUrlStrategyConfig {

	@Bean
	public Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy(AnimediaEpisodeUrlService animediaEpisodeUrlService,
			NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService, AnidubEpisodeUrlService anidubEpisodeUrlService,
			JisedaiEpisodeUrlService jisedaiEpisodeUrlService, AnimepikEpisodeUrlService animepikEpisodeUrlService,
			AnilibriaEpisodeUrlService anilibriaEpisodeUrlService, JutsuEpisodeUrlService jutsuEpisodeUrlService,
			SovetRomanticaEpisodeUrlService sovetRomanticaEpisodeUrlService) {
		EnumMap<FanDubSource, EpisodeUrlServiceI> map = new EnumMap<>(FanDubSource.class);
		map.put(ANIMEDIA, animediaEpisodeUrlService);
		map.put(NINEANIME, nineAnimeEpisodeUrlService);
		map.put(ANIDUB, anidubEpisodeUrlService);
		map.put(JISEDAI, jisedaiEpisodeUrlService);
		map.put(ANIMEPIK, animepikEpisodeUrlService);
		map.put(ANILIBRIA, anilibriaEpisodeUrlService);
		map.put(JUTSU, jutsuEpisodeUrlService);
		map.put(SOVETROMANTICA, sovetRomanticaEpisodeUrlService);
		return map;
	}
}
