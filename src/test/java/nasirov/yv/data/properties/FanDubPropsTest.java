package nasirov.yv.data.properties;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import nasirov.yv.AbstractTest;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class FanDubPropsTest extends AbstractTest {

	@Test
	public void shouldReturnUrls() {
		//given
		//when
		Map<FanDubSource, String> result = fanDubProps.getUrls();
		//then
		assertEquals("https://online.animedia.tv/", result.get(FanDubSource.ANIMEDIA));
		assertEquals("https://anime.anidub.life/", result.get(FanDubSource.ANIDUB));
		assertEquals("https://www.anilibria.tv/", result.get(FanDubSource.ANILIBRIA));
		assertEquals("https://animepik.org/", result.get(FanDubSource.ANIMEPIK));
		assertEquals("https://jisedai.tv/", result.get(FanDubSource.JISEDAI));
		assertEquals("https://9anime.to", result.get(FanDubSource.NINEANIME));
		assertEquals("https://jut.su/", result.get(FanDubSource.JUTSU));
	}
}