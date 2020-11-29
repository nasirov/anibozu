package nasirov.yv.service;


import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * @author Nasirov Yuriy
 */
public interface EpisodeUrlServiceI {

	/**
	 * Searches for a new episode URL based on an user watching title
	 *
	 * @param watchingTitle an user currently watching title
	 * @return 1. a new episode is available - actual URL to a FanDub site
	 * <p>
	 * 2. a new episode is not available - {@link nasirov.yv.data.constants.BaseConstants#FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE}
	 * <p>
	 * 3. a title is not found - {@link nasirov.yv.data.constants.BaseConstants#NOT_FOUND_ON_FANDUB_SITE_URL}
	 */
	String getEpisodeUrl(FanDubSource fanDubSource, MalTitle watchingTitle);
}
