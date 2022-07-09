package nasirov.yv.service;

import nasirov.yv.data.front.UserInputDto;

/**
 * @author Nasirov Yuriy
 */
public interface CacheCleanerServiceI {

	void clearSseCache(UserInputDto userInputDto);
}
