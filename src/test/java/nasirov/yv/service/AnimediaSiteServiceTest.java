package nasirov.yv.service;

import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static nasirov.yv.utils.TestConstants.TEXT_HTML_CHARSET_UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaSiteServiceTest extends AbstractTest {

	@Test
	public void getAnimediaSearchList() {
		createStubWithBodyFile("/ajax/search_result_search_page_2/P0search&limit=2", TEXT_HTML_CHARSET_UTF_8, "animedia/search/aslFromSite.html");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaSiteService.getAnimediaSearchList();
		assertEquals(2, animediaSearchList.size());
	}

	@Test
	public void getDataLists() {
		createStubWithBodyFile("/" + REGULAR_TITLE_URL, TEXT_HTML_CHARSET_UTF_8, "animedia/regular/regularTitle.html");
		List<String> dataLists = animediaSiteService.getDataLists(buildAnimediaSearchListTitle());
		List<String> expectedSeasons = expectedSeasons();
		assertEquals(expectedSeasons.size(), dataLists.size());
		expectedSeasons.forEach(x -> assertTrue(dataLists.contains(x)));
	}

	@Test
	public void getDataListEpisodes() {
		String dataList = "1";
		createStubWithBodyFile("/embeds/playlist-j.txt/" + REGULAR_TITLE_ID + "/" + dataList,
				TEXT_HTML_CHARSET_UTF_8,
				"animedia/regular" + "/regularTitleDataList1.html");
		List<String> dataListEpisodes = animediaSiteService.getEpisodes(REGULAR_TITLE_ID, dataList);
		List<String> expectedEpisodes = expectedEpisodes();
		assertEquals(expectedEpisodes.size(), dataListEpisodes.size());
		expectedEpisodes.forEach(x -> assertTrue(dataListEpisodes.contains(x)));
	}

	private List<String> expectedEpisodes() {
		return Lists.newArrayList("Серия 1", "Серия 2", "Серия 3", "Серия 4", "Серия 5");
	}

	private List<String> expectedSeasons() {
		return Lists.newArrayList("1", "2", "3", "7");
	}

	private AnimediaSearchListTitle buildAnimediaSearchListTitle() {
		return AnimediaSearchListTitle.builder()
				.animeId(REGULAR_TITLE_ID)
				.url(REGULAR_TITLE_URL)
				.build();
	}
}