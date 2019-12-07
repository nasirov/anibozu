package nasirov.yv.service;

import static nasirov.yv.data.constants.CacheNamesConstants.CURRENTLY_UPDATED_TITLES_CACHE;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.TEXT_JAVASCRIPT_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.TEXT_PLAIN_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */

public class AnimediaServiceTest extends AbstractTest {

	private static final String POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER = "h=350&q=100";

	private static final int CURRENTLY_UPDATED_SIZE = 10;

	@Test
	public void testGetAnimediaSearchListFromAnimedia() {
		createStubWithBodyFile("/ajax/anime_list", TEXT_JAVASCRIPT_CHARSET_UTF_8, "animedia/search/animediaSearchListFull.json");
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromAnimedia();
		assertEquals(3, animediaSearchList.size());
		assertTrue(animediaSearchList.stream()
				.allMatch(set -> set.getUrl()
						.matches("^anime/.+") && set.getPosterUrl()
						.matches("https://static\\.animedia\\.tv/uploads/.+\\?" + POSTER_URL_HIGH_QUALITY_QUERY_PARAMETER)));
	}

	@Test
	public void testGetAnimediaSearchListFromGitHub() {
		createStubWithBodyFile("/nasirov/anime-checker-resources/master/animediaSearchList.json",
				TEXT_PLAIN_CHARSET_UTF_8,
				"github/animediaSearchListFull.json");
		Set<AnimediaTitleSearchInfo> animediaSearchList = animediaService.getAnimediaSearchListFromGitHub();
		assertEquals(3, animediaSearchList.size());
	}

	@Test
	public void testGetCurrentlyUpdatedTitles() {
		stubCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		assertEquals(CURRENTLY_UPDATED_SIZE, currentlyUpdatedTitles.size());
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFromCache = getAnimediaMALTitleReferencesFromCache();
		assertEquals(CURRENTLY_UPDATED_SIZE, animediaMALTitleReferencesFromCache.size());
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesDifferentValues() {
		stubCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> freshCurrentlyUpdatedTitles = createFreshCurrentlyUpdatedTitles(currentlyUpdatedTitles);
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(freshCurrentlyUpdatedTitles, currentlyUpdatedTitles);
		assertEquals(1, result.size());
		List<AnimediaMALTitleReferences> cachedCurrentlyUpdatedTitles = getAnimediaMALTitleReferencesFromCache();
		assertEquals(freshCurrentlyUpdatedTitles, cachedCurrentlyUpdatedTitles);
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmpty() {
		stubCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> cachedCurrentlyUpdatedTitles = getAnimediaMALTitleReferencesFromCache();
		assertEquals(currentlyUpdatedTitles, cachedCurrentlyUpdatedTitles);
		List<AnimediaMALTitleReferences> emptyCurrentlyUpdatedTitles = new ArrayList<>();
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(currentlyUpdatedTitles, emptyCurrentlyUpdatedTitles);
		assertTrue(result.isEmpty());
		cachedCurrentlyUpdatedTitles = getAnimediaMALTitleReferencesFromCache();
		assertEquals(CURRENTLY_UPDATED_SIZE, cachedCurrentlyUpdatedTitles.size());
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesFreshEmpty() {
		stubCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> currentlyUpdatedTitles = animediaService.getCurrentlyUpdatedTitles();
		List<AnimediaMALTitleReferences> cachedCurrentlyUpdatedTitles = getAnimediaMALTitleReferencesFromCache();
		assertEquals(currentlyUpdatedTitles, cachedCurrentlyUpdatedTitles);
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(new ArrayList<>(), currentlyUpdatedTitles);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testCheckCurrentlyUpdatedTitlesCacheEmptyFreshEmpty() {
		List<AnimediaMALTitleReferences> result = animediaService.checkCurrentlyUpdatedTitles(new ArrayList<>(), new ArrayList<>());
		assertTrue(result.isEmpty());
		List<AnimediaMALTitleReferences> resultFromCache = getAnimediaMALTitleReferencesFromCache();
		assertNull(resultFromCache);
	}

	private void stubCurrentlyUpdatedTitles() {
		createStubWithBodyFile("/", TEXT_HTML_CHARSET_UTF_8, "animedia/search/animediaMainPage.txt");
	}

	private List<AnimediaMALTitleReferences> createFreshCurrentlyUpdatedTitles(List<AnimediaMALTitleReferences> currentlyUpdatedTitles) {
		List<AnimediaMALTitleReferences> animediaMALTitleReferencesFresh = new ArrayList<>();
		currentlyUpdatedTitles.forEach(list -> animediaMALTitleReferencesFresh.add(new AnimediaMALTitleReferences(list)));
		animediaMALTitleReferencesFresh.add(0, animediaMALTitleReferencesFresh.get(9));
		animediaMALTitleReferencesFresh.remove(9);
		return animediaMALTitleReferencesFresh;
	}

	private List getAnimediaMALTitleReferencesFromCache() {
		return currentlyUpdatedTitlesCache.get(CURRENTLY_UPDATED_TITLES_CACHE, ArrayList.class);
	}
}