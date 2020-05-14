package nasirov.yv.util;

import static nasirov.yv.utils.AnimediaTitlesTestBuilder.buildConcretizedAndOngoingAnimediaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.buildUpdatedRegularAnimediaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getConcretizedAnimediaTitleWithEpisodesRange;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getNotFoundOnMalAnimediaTitle;
import static nasirov.yv.utils.AnimediaTitlesTestBuilder.getRegularNotUpdatedAnimediaTitle;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaUtilsTest {

	@Test
	public void isMaxEpisodesUndefined() {
		String[] undefinedPositiveVariants = {"x", "xx", "xxx", "X", "XX", "XXX", "х", "хх", "ххх", "Х", "ХХ", "ХХХ"};
		String[] undefinedNegativeVariants = {"", "xxxx", " ", "XXXX", "хххх", "ХХХХ"};
		for (String var : undefinedPositiveVariants) {
			assertTrue(AnimediaUtils.isMaxEpisodeUndefined(var));
		}
		for (String var : undefinedNegativeVariants) {
			assertFalse(AnimediaUtils.isMaxEpisodeUndefined(var));
		}
	}

	@Test
	public void isTitleConcretizedAndOngoing() {
		assertTrue(AnimediaUtils.isTitleConcretizedAndOngoing(buildConcretizedAndOngoingAnimediaTitle()));
		assertFalse(AnimediaUtils.isTitleConcretizedAndOngoing(getConcretizedAnimediaTitleWithEpisodesRange()));
	}
	@Test
	public void isTitleConcretizedOnMAL() {
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(buildConcretizedAndOngoingAnimediaTitle()));
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(getConcretizedAnimediaTitleWithEpisodesRange()));
	}
	@Test
	public void isTitleUpdated() {
		assertTrue(AnimediaUtils.isTitleUpdated(buildUpdatedRegularAnimediaTitle()));
		assertFalse(AnimediaUtils.isTitleUpdated(getRegularNotUpdatedAnimediaTitle()));
	}
	@Test
	public void isTitleNotFoundOnMAL() {
		assertTrue(AnimediaUtils.isTitleNotFoundOnMAL(getNotFoundOnMalAnimediaTitle()));
		assertFalse(AnimediaUtils.isTitleNotFoundOnMAL(getConcretizedAnimediaTitleWithEpisodesRange()));
	}
	@Test
	public void getCorrectCurrentMax() {
		String currentMax = "13";
		String joinedEpisode = "12-" + currentMax;
		assertEquals(currentMax, AnimediaUtils.getCorrectCurrentMax(joinedEpisode));
		assertEquals(currentMax, AnimediaUtils.getCorrectCurrentMax(currentMax));
	}

	@Test
	public void getCorrectFirstEpisodeAndMin() {
		String secondEpisode = "2";
		String firstEpisode = "1";
		String joinedEpisode = firstEpisode + "-" + secondEpisode;
		assertEquals(firstEpisode, AnimediaUtils.getCorrectFirstEpisodeAndMin(joinedEpisode));
		assertEquals(firstEpisode, AnimediaUtils.getCorrectFirstEpisodeAndMin(firstEpisode));
	}

	@Test
	public void splitJoinedEpisodes() {
		assertArrayEquals(new String[]{"1", "2"}, AnimediaUtils.splitJoinedEpisodes("1-2"));
		assertArrayEquals(new String[]{"1"}, AnimediaUtils.splitJoinedEpisodes("1"));
	}

	@Test
	public void isJoinedEpisodes() {
		assertFalse(AnimediaUtils.isJoinedEpisodes("1"));
		assertTrue(AnimediaUtils.isJoinedEpisodes("1-2"));
		assertTrue(AnimediaUtils.isJoinedEpisodes("123-321"));
	}

	@Test
	public void testForbiddenPrivateConstructor() throws ReflectiveOperationException {
		Constructor<?>[] declaredConstructors = AnimediaUtils.class.getDeclaredConstructors();
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