package nasirov.yv.service;

import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface EpisodeUrlServiceI {

	/**
	 * Searches for a new episode URL based on a user watching title
	 *
	 * @param watchingTitle an user currently watching title
	 * @return 1. a new episode is available - actual URL to a FunDub site
	 * 2. a new episode is not available - {@link nasirov.yv.data.constants.BaseConstants#FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE}
	 * 3. a title is not found - {@link nasirov.yv.data.constants.BaseConstants#NOT_FOUND_ON_FUNDUB_SITE_URL}
	 */
	String getEpisodeUrl(UserMALTitleInfo watchingTitle);
}
