package nasirov.yv.configuration;

import static nasirov.yv.data.constants.FunDubSource.ANIMEDIA;
import static nasirov.yv.data.constants.FunDubSource.NINEANIME;

import java.util.EnumMap;
import java.util.Map;
import nasirov.yv.data.constants.FunDubSource;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.impl.AnimediaEpisodeUrlService;
import nasirov.yv.service.impl.NineAnimeEpisodeUrlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
public class EpisodeUrlStrategyConfig {

	@Bean
	public Map<FunDubSource, EpisodeUrlServiceI> episodeUrlStrategy(AnimediaEpisodeUrlService animediaEpisodeUrlService,
			NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService) {
		EnumMap<FunDubSource, EpisodeUrlServiceI> map = new EnumMap<>(FunDubSource.class);
		map.put(ANIMEDIA, animediaEpisodeUrlService);
		map.put(NINEANIME, nineAnimeEpisodeUrlService);
		return map;
	}
}