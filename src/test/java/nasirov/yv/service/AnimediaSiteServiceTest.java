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
import nasirov.yv.data.animedia.api.Response;
import nasirov.yv.data.animedia.api.Season;
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
		Response titleInfo = animediaSiteService.getDataLists(buildAnimediaSearchListTitle());
		List<Season> seasons = titleInfo.getSeasons();
		List<Season> expectedSeasons = expectedSeasons();
		assertEquals(expectedSeasons.size(), seasons.size());
		expectedSeasons.forEach(x -> assertTrue(seasons.contains(x)));
	}

	@Test
	public void getDataListEpisodes() {
		String dataList = "1";
		createStubWithBodyFile("/embeds/playlist-j.txt/" + REGULAR_TITLE_ID + "/" + dataList,
				TEXT_HTML_CHARSET_UTF_8,
				"animedia/regular" + "/regularTitleDataList1.html");
		List<Response> dataListInfo = animediaSiteService.getDataListEpisodes(REGULAR_TITLE_ID, dataList);
		List<String> expectedEpisodes = expectedEpisodes();
		assertEquals(expectedEpisodes.size(), dataListInfo.size());
		for (int i = 0; i < expectedEpisodes.size(); i++) {
			assertEquals(expectedEpisodes.get(i),
					dataListInfo.get(i)
							.getEpisodeName());
		}
	}

	private List<String> expectedEpisodes() {
		return Lists.newArrayList("Серия 1", "Серия 2", "Серия 3", "Серия 4", "Серия 5");
	}

	private List<Season> expectedSeasons() {
		return Lists.newArrayList(new Season("1"), new Season("2"), new Season("3"), new Season("7"));
	}

	private AnimediaSearchListTitle buildAnimediaSearchListTitle() {
		return AnimediaSearchListTitle.builder()
				.animeId(REGULAR_TITLE_ID)
				.url(REGULAR_TITLE_URL)
				.build();
	}
}