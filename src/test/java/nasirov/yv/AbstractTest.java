package nasirov.yv;

import java.util.Objects;
import nasirov.yv.fandub.service.spring.boot.starter.feign.mal_service.MalServiceFeignClient;
import nasirov.yv.service.SseEmitterExecutorServiceI;
import nasirov.yv.service.impl.common.CacheCleanerService;
import nasirov.yv.service.impl.fandub.nine_anime.NineAnimeEpisodeUrlService;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Nasirov Yuriy
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public abstract class AbstractTest {

	@MockBean
	protected MalServiceFeignClient malServiceFeignClient;

	@SpyBean
	protected SseEmitterExecutorServiceI sseEmitterExecutorService;

	@Autowired
	protected NineAnimeEpisodeUrlService nineAnimeEpisodeUrlService;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected CacheCleanerService cacheCleanerService;

	@After
	public void tearDown() {
		clearCaches();
	}

	private void clearCaches() {
		cacheManager.getCacheNames()
				.stream()
				.map(cacheManager::getCache)
				.filter(Objects::nonNull)
				.forEach(Cache::clear);
	}
}
