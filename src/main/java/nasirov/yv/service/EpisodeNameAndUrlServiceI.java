package nasirov.yv.service;


import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface EpisodeNameAndUrlServiceI {

	/**
	 * Searches for a new episode name and URL based on a user watching title
	 *
	 * @param watchingTitle a user currently watching title
	 * @return 1. new episode is available - name and actual URL to a FanDub site
	 * <p>
	 * 2. new episode is not available - {@link nasirov.yv.data.constants.BaseConstants#NOT_AVAILABLE_EPISODE_NAME_AND_URL}
	 * <p>
	 * 3. a title is not found - {@link nasirov.yv.data.constants.BaseConstants#TITLE_NOT_FOUND_EPISODE_NAME_AND_URL}
	 * <p>
	 * wrapped with {@link Mono}
	 */
	Mono<Pair<String, String>> getEpisodeNameAndUrl(MalTitle watchingTitle);
}
