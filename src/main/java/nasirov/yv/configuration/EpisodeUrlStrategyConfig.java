package nasirov.yv.configuration;


import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JISEDAI;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JUTSU;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SHIZAPROJECT;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SOVETROMANTICA;

import java.util.EnumMap;
import java.util.Map;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.fandub.AnidubEpisodeUrlService;
import nasirov.yv.service.impl.fandub.AnilibriaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.fandub.AnimepikEpisodeUrlService;
import nasirov.yv.service.impl.fandub.JisedaiEpisodeUrlService;
import nasirov.yv.service.impl.fandub.JutsuEpisodeUrlService;
import nasirov.yv.service.impl.fandub.NineAnimeEpisodeUrlService;
import nasirov.yv.service.impl.fandub.ShizaProjectEpisodeUrlService;
import nasirov.yv.service.impl.fandub.SovetRomanticaEpisodeUrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nasirov Yuriy
 */
@Configuration
public class EpisodeUrlStrategyConfig {

	@Bean
	public Map<FanDubSource, EpisodeUrlServiceI> episodeUrlStrategy(AnimediaEpisodeUrlService animediaEpisodeUrlService,
			NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService, AnidubEpisodeUrlService anidubEpisodeUrlService,
			JisedaiEpisodeUrlService jisedaiEpisodeUrlService, AnimepikEpisodeUrlService animepikEpisodeUrlService,
			AnilibriaEpisodeUrlService anilibriaEpisodeUrlService, JutsuEpisodeUrlService jutsuEpisodeUrlService,
			SovetRomanticaEpisodeUrlService sovetRomanticaEpisodeUrlService, ShizaProjectEpisodeUrlService shizaProjectEpisodeUrlService) {
		EnumMap<FanDubSource, EpisodeUrlServiceI> map = new EnumMap<>(FanDubSource.class);
		map.put(ANIMEDIA, animediaEpisodeUrlService);
		map.put(NINEANIME, nineAnimeEpisodeUrlService);
		map.put(ANIDUB, anidubEpisodeUrlService);
		map.put(JISEDAI, jisedaiEpisodeUrlService);
		map.put(ANIMEPIK, animepikEpisodeUrlService);
		map.put(ANILIBRIA, anilibriaEpisodeUrlService);
		map.put(JUTSU, jutsuEpisodeUrlService);
		map.put(SOVETROMANTICA, sovetRomanticaEpisodeUrlService);
		map.put(SHIZAPROJECT, shizaProjectEpisodeUrlService);
		return map;
	}
}
