package nasirov.yv.service;

import static nasirov.yv.utils.TestConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static org.apache.groovy.util.Maps.of;
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
public class AnimediaApiServiceTest extends AbstractTest {

	@Test
	public void getAnimediaSearchList() {
		createStubWithBodyFile("/api/anime-list/1", APPLICATION_JSON_CHARSET_UTF_8, "animedia/search/aslPart1.json");
		createStubWithBodyFile("/api/anime-list/2", APPLICATION_JSON_CHARSET_UTF_8, "animedia/search/aslPart2.json");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaApiService.getAnimediaSearchList();
		assertEquals(2, animediaSearchList.size());
	}

	@Test
	public void getTitleInfo() {
		stubAnimeMainPageAndDataLists(REGULAR_TITLE_ID, "animedia/regular/regularTitle.json", of("1", "animedia/regular/regularTitleDataList1.json"));
		Response titleInfo = animediaApiService.getTitleInfo(REGULAR_TITLE_ID);
		List<Season> seasons = titleInfo.getSeasons();
		List<Season> expectedSeasons = expectedSeasons();
		assertEquals(expectedSeasons.size(), seasons.size());
		expectedSeasons.forEach(x -> assertTrue(seasons.contains(x)));
	}

	@Test
	public void getDataListInfo() {
		String dataList = "1";
		stubAnimeMainPageAndDataLists(REGULAR_TITLE_ID,
				"animedia/regular/regularTitle.json",
				of(dataList, "animedia/regular/regularTitleDataList1.json"));
		List<Response> dataListInfo = animediaApiService.getDataListInfo(REGULAR_TITLE_ID, dataList);
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
		return Lists.newArrayList(new Season("1 Сезон", "1"), new Season("2 Сезон", "2"), new Season("3 Сезон", "3"), new Season("ОВА & ODA", "7"));
	}
}