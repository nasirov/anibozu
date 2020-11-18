package nasirov.yv.service;

import nasirov.yv.data.front.UserInputDto;

/**
 * Created by nasirov.yv
 */
public interface CacheCleanerServiceI {

	/**
	 * Evicts sse cache
	 *
	 * @param userInputDto cache key
	 */
	void clearSseCache(UserInputDto userInputDto);
}
