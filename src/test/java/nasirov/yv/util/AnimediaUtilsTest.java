package nasirov.yv.util;

import static nasirov.yv.TestUtils.getEpisodesRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.constants.BaseConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by nasirov.yv
 */

@RunWith(SpringRunner.class)
public class AnimediaUtilsTest {

	@Value("classpath:referencesForTest.json")
	private Resource referencesForTestResource;

	@Value("classpath:animedia/announcements/htmlWithAnnouncement.txt")
	private Resource htmlWithAnnouncement;

	@Value("classpath:animedia/sao/saoHtml.txt")
	private Resource saoHtml;

	private AnimediaMALTitleReferences concretizedAndOngoing;

	private AnimediaMALTitleReferences concretizedAndNotOngoing;

	private AnimediaMALTitleReferences updatedTitle;

	private AnimediaMALTitleReferences notUpdatedTitle;

	private AnimediaMALTitleReferences notFoundOnMAL;

	private static final String ANIME_ID = "1234";
	private static final String DATA_LIST = "1";
	private static final String MAX_EPISODES = "12";

	private Map<String, Map<String, String>> animeIdDataListsAndMaxEpisodesMapForTest;

	private Map<String, String> dataListsAndMaxEpisodesMapForTest;



	@Before
	public void setUp() {
		Set<AnimediaMALTitleReferences> references = RoutinesIO
				.unmarshalFromResource(referencesForTestResource, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		concretizedAndOngoing = references.stream().filter(ref -> ref.getTitleOnMAL().equals("shingeki no kyojin season 3 part 2")).findFirst()
				.orElse(null);
		concretizedAndNotOngoing = references.stream().filter(ref -> ref.getTitleOnMAL().equals("one punch man: road to hero")).findFirst().orElse(null);
		updatedTitle = AnimediaMALTitleReferences.builder().url("anime/url").dataList("1").minConcretizedEpisodeOnAnimedia("1").titleOnMAL("titleName")
				.firstEpisode("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175").posterUrl("posterUrl").episodesRange(getEpisodesRange("1",
						"175"))
				.build();
		notUpdatedTitle = references.stream().filter(ref -> ref.getTitleOnMAL().equals("fairy tail: final series")).findFirst().orElse(null);
		notFoundOnMAL = references.stream().filter(ref -> ref.getTitleOnMAL().equals(BaseConstants.NOT_FOUND_ON_MAL)).findFirst().orElse(null);
		animeIdDataListsAndMaxEpisodesMapForTest = new HashMap<>();
		dataListsAndMaxEpisodesMapForTest = new HashMap<>();
		dataListsAndMaxEpisodesMapForTest.put(DATA_LIST, MAX_EPISODES);
		animeIdDataListsAndMaxEpisodesMapForTest.put(ANIME_ID, dataListsAndMaxEpisodesMapForTest);
	}

	@Test
	public void isMaxEpisodesUndefined() throws Exception {
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
	public void isAnnouncement() throws Exception {
		assertTrue(AnimediaUtils.isAnnouncement(RoutinesIO.readFromResource(htmlWithAnnouncement)));
		assertFalse(AnimediaUtils.isAnnouncement(RoutinesIO.readFromResource(saoHtml)));
	}
	@Test
	public void isTitleConcretizedAndOngoing() throws Exception {
		assertTrue(AnimediaUtils.isTitleConcretizedAndOngoing(concretizedAndOngoing));
		assertFalse(AnimediaUtils.isTitleConcretizedAndOngoing(concretizedAndNotOngoing));
	}
	@Test
	public void isTitleConcretizedOnMAL() throws Exception {
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(concretizedAndOngoing));
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(concretizedAndNotOngoing));
	}
	@Test
	public void isTitleUpdated() throws Exception {
		assertTrue(AnimediaUtils.isTitleUpdated(updatedTitle));
		assertFalse(AnimediaUtils.isTitleUpdated(notUpdatedTitle));
	}
	@Test
	public void isTitleNotFoundOnMAL() throws Exception {
		assertTrue(AnimediaUtils.isTitleNotFoundOnMAL(notFoundOnMAL));
		assertFalse(AnimediaUtils.isTitleNotFoundOnMAL(concretizedAndNotOngoing));
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
	public void testForbiddenPrivateConstructor() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<?>[] declaredConstructors = AnimediaUtils.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		try {
			declaredConstructors[0].newInstance();
		} catch (InvocationTargetException e) {
			assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
		}
	}

	@Test
	public void testGetAnimeId() {
		String animeId = AnimediaUtils.getAnimeId(animeIdDataListsAndMaxEpisodesMapForTest);
		assertEquals(ANIME_ID, animeId);
	}

	@Test
	public void testGetDataListsAndMaxEpisodesMap() {
		Map<String, String> dataListsAndMaxEpisodesMap = AnimediaUtils.getDataListsAndMaxEpisodesMap(animeIdDataListsAndMaxEpisodesMapForTest);
		assertEquals(animeIdDataListsAndMaxEpisodesMapForTest.size(),dataListsAndMaxEpisodesMap.size());
		assertEquals(dataListsAndMaxEpisodesMapForTest, dataListsAndMaxEpisodesMap);
	}
}