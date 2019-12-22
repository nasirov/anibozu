package nasirov.yv.util;

import static nasirov.yv.utils.ReferencesBuilder.buildConcretizedAndOngoingReference;
import static nasirov.yv.utils.ReferencesBuilder.buildConcretizedReferenceWithEpisodesRange;
import static nasirov.yv.utils.ReferencesBuilder.buildUpdatedRegularReference;
import static nasirov.yv.utils.ReferencesBuilder.getRegularReferenceNotUpdated;
import static nasirov.yv.utils.ReferencesBuilder.notFoundOnAnimedia;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import nasirov.yv.AbstractTest;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaUtilsTest extends AbstractTest {

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
		assertTrue(AnimediaUtils.isTitleConcretizedAndOngoing(buildConcretizedAndOngoingReference()));
		assertFalse(AnimediaUtils.isTitleConcretizedAndOngoing(buildConcretizedReferenceWithEpisodesRange()));
	}
	@Test
	public void isTitleConcretizedOnMAL() {
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(buildConcretizedAndOngoingReference()));
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(buildConcretizedReferenceWithEpisodesRange()));
	}
	@Test
	public void isTitleUpdated() {
		assertTrue(AnimediaUtils.isTitleUpdated(buildUpdatedRegularReference()));
		assertFalse(AnimediaUtils.isTitleUpdated(getRegularReferenceNotUpdated()));
	}
	@Test
	public void isTitleNotFoundOnMAL() {
		assertTrue(AnimediaUtils.isTitleNotFoundOnMAL(notFoundOnAnimedia()));
		assertFalse(AnimediaUtils.isTitleNotFoundOnMAL(buildConcretizedReferenceWithEpisodesRange()));
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