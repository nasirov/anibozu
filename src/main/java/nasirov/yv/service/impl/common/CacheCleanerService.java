package nasirov.yv.service.impl.common;

import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.CacheCleanerServiceI;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Service
public class CacheCleanerService implements CacheCleanerServiceI {

	@Override
	@CacheEvict(cacheNames = "sse", key = "#userInputDto.getUsername() + ':' +#userInputDto.getFanDubSources()")
	public void clearSseCache(UserInputDto userInputDto) {
	}
}
