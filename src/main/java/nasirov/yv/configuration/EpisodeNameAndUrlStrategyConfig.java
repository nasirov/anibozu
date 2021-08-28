package nasirov.yv.configuration;


import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIDUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANILIBRIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEDIA;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANIMEPIK;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.ANYTHING_GROUP;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JAMCLUB;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JISEDAI;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.JUTSU;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.NINEANIME;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SHIZAPROJECT;
import static nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource.SOVETROMANTICA;

import java.util.EnumMap;
import java.util.Map;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.EpisodeNameAndUrlServiceI;
import nasirov.yv.service.impl.fandub.AnidubEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnilibriaEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnimediaEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnimepikEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.AnythingGroupEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.JamClubEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.JisedaiEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.JutsuEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.NineAnimeEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.ShizaProjectEpisodeNameAndUrlService;
import nasirov.yv.service.impl.fandub.SovetRomanticaEpisodeNameAndUrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nasirov Yuriy
 */
@Configuration
public class EpisodeNameAndUrlStrategyConfig {

	@Bean
	public Map<FanDubSource, EpisodeNameAndUrlServiceI> episodeUrlStrategy(AnimediaEpisodeNameAndUrlService animediaEpisodeNameAndUrlService,
			NineAnimeEpisodeNameAndUrlService nineAnimeEpisodeNameAndUrlService, AnidubEpisodeNameAndUrlService anidubEpisodeNameAndUrlService,
			JisedaiEpisodeNameAndUrlService jisedaiEpisodeNameAndUrlService, AnimepikEpisodeNameAndUrlService animepikEpisodeNameAndUrlService,
			AnilibriaEpisodeNameAndUrlService anilibriaEpisodeNameAndUrlService, JutsuEpisodeNameAndUrlService jutsuEpisodeNameAndUrlService,
			SovetRomanticaEpisodeNameAndUrlService sovetRomanticaEpisodeNameAndUrlService,
			ShizaProjectEpisodeNameAndUrlService shizaProjectEpisodeNameAndUrlService, JamClubEpisodeNameAndUrlService jamClubEpisodeNameAndUrlService,
			AnythingGroupEpisodeNameAndUrlService anythingGroupEpisodeNameAndUrlService) {
		EnumMap<FanDubSource, EpisodeNameAndUrlServiceI> map = new EnumMap<>(FanDubSource.class);
		map.put(ANIMEDIA, animediaEpisodeNameAndUrlService);
		map.put(NINEANIME, nineAnimeEpisodeNameAndUrlService);
		map.put(ANIDUB, anidubEpisodeNameAndUrlService);
		map.put(JISEDAI, jisedaiEpisodeNameAndUrlService);
		map.put(ANIMEPIK, animepikEpisodeNameAndUrlService);
		map.put(ANILIBRIA, anilibriaEpisodeNameAndUrlService);
		map.put(JUTSU, jutsuEpisodeNameAndUrlService);
		map.put(SOVETROMANTICA, sovetRomanticaEpisodeNameAndUrlService);
		map.put(SHIZAPROJECT, shizaProjectEpisodeNameAndUrlService);
		map.put(JAMCLUB, jamClubEpisodeNameAndUrlService);
		map.put(ANYTHING_GROUP, anythingGroupEpisodeNameAndUrlService);
		return map;
	}
}
