package nasirov.yv.main;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import nasirov.yv.AbstractTest;
import nasirov.yv.AnimeCheckerApplication;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created by nasirov.yv
 */
@SpringBootTest
public class AnimeCheckerApplicationTest extends AbstractTest {

	@Test
	public void testMain() throws Exception {
		AnimeCheckerApplication.main(new String[0]);
		assertEquals(1,
				Stream.of(Thread.getAllStackTraces()).flatMap(x -> x.entrySet().stream()).filter(x -> x.getKey().getName().matches("main")).count());
	}

}