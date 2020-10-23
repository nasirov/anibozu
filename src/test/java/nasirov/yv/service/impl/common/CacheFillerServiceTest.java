package nasirov.yv.service.impl.common;

import static org.mockito.Mockito.verify;

import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.TitlesServiceI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheFillerServiceTest {

	@Mock
	private TitlesServiceI titlesService;

	@InjectMocks
	private CacheFillerService cacheFillerService;

	@Test
	public void shouldGetTitlesByAllFandubSourcesAndFillGithubCache() {
		//given
		FanDubSource[] fanDubSources = FanDubSource.values();
		//when
		cacheFillerService.fillGithubCache();
		//then
		for (FanDubSource fanDubSource : fanDubSources) {
			verify(titlesService).getTitles(fanDubSource);
		}
	}
}