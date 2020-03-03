package nasirov.yv.service;

import nasirov.yv.data.mal.MALUser;

/**
 * Created by nasirov.yv
 */
public interface CacheCleanerServiceI {

	/**
	 * Evicts sse cache
	 *
	 * @param malUser cache key
	 */
	void clearSseCache(MALUser malUser);
}
