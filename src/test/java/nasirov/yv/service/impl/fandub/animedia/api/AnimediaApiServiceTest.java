package nasirov.yv.service.impl.fandub.animedia.api;

import static nasirov.yv.utils.TestConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.apache.groovy.util.Maps.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.animedia.AnimediaSearchListTitle;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaApiServiceTest extends AbstractTest {

	private AnimediaApiService animediaApiService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		animediaApiService = new AnimediaApiService(animediaApiFeignClient);
	}

	@Test
	public void getAnimediaSearchList() {
		createStubWithBodyFile("/api/anime-list/1", APPLICATION_JSON_CHARSET_UTF_8, "animedia/search/aslPart1.json");
		createStubWithBodyFile("/api/anime-list/2", APPLICATION_JSON_CHARSET_UTF_8, "animedia/search/aslPart2.json");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaApiService.getAnimediaSearchList();
		assertEquals(2, animediaSearchList.size());
	}

	@Test
	public void getDataLists() {
		stubAnimeMainPageAndDataLists(REGULAR_TITLE_ID, "animedia/regular/regularTitle.json", of("1", "animedia/regular/regularTitleDataList1.json"));
		List<String> dataLists = animediaApiService.getDataLists(buildAnimediaSearchListTitle());
		List<String> expectedDataLists = expectedDataLists();
		assertEquals(expectedDataLists.size(), dataLists.size());
		expectedDataLists.forEach(x -> assertTrue(dataLists.contains(x)));
	}

	@Test
	public void getDataListEpisodes() {
		String dataList = "1";
		stubAnimeMainPageAndDataLists(REGULAR_TITLE_ID,
				"animedia/regular/regularTitle.json",
				of(dataList, "animedia/regular/regularTitleDataList1.json"));
		List<String> dataListEpisodes = animediaApiService.getEpisodes(REGULAR_TITLE_ID, dataList);
		List<String> expectedEpisodes = expectedEpisodes();
		assertEquals(expectedEpisodes.size(), dataListEpisodes.size());
		expectedEpisodes.forEach(x -> assertTrue(dataListEpisodes.contains(x)));
	}

	private List<String> expectedEpisodes() {
		return Lists.newArrayList("Серия 1", "Серия 2", "Серия 3", "Серия 4", "Серия 5");
	}

	private List<String> expectedDataLists() {
		return Lists.newArrayList("1", "2", "3", "7");
	}

	private AnimediaSearchListTitle buildAnimediaSearchListTitle() {
		return AnimediaSearchListTitle.builder()
				.animeId(REGULAR_TITLE_ID)
				.url(REGULAR_TITLE_URL)
				.build();
	}
}