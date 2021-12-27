package nasirov.yv.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.SneakyThrows;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import org.junit.jupiter.api.Test;

/**
 * @author Nasirov Yuriy
 */
class MalUtilsTest {

	@Test
	void shouldReturnNextEpisodeForWatch() {
		//given
		MalTitle malTitle = MalTitle.builder()
				.id(1)
				.numWatchedEpisodes(0)
				.build();
		//when
		Integer nextEpisodeForWatch = MalUtils.getNextEpisodeForWatch(malTitle);
		//then
		assertEquals(1, nextEpisodeForWatch.intValue());
		assertEquals("1", nextEpisodeForWatch.toString());
	}

	@Test
	@SneakyThrows
	void shouldFailOnPrivateConstructor() {
		Constructor<?>[] declaredConstructors = MalUtils.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		try {
			declaredConstructors[0].newInstance();
		} catch (InvocationTargetException e) {
			assertEquals(UnsupportedOperationException.class,
					e.getCause()
							.getClass());
		}
	}
}