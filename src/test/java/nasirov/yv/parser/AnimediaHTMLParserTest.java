package nasirov.yv.parser;

import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.constants.BaseConstants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by nasirov.yv
 */

public class AnimediaHTMLParserTest extends AbstractTest {

	@Autowired
	private AnimediaHTMLParser animediaHTMLParser;

	@Test
	public void testGetAnimeIdSeasonsAndEpisodesMap() {
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap =
				animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(routinesIO.readFromResource(
				saoHtml));
		assertNotNull(animeIdSeasonsAndEpisodesMap);
		List<String> dataLists = new ArrayList<>();
		List<String> maxEpisode = new ArrayList<>();
		for (Map.Entry<String, Map<String, String>> entry : animeIdSeasonsAndEpisodesMap.entrySet()) {
			assertEquals(MULTI_SEASONS_TITLE_ID, entry.getKey());
			for (Map.Entry<String, String> dataListEpisode : entry.getValue()
					.entrySet()) {
				maxEpisode.add(dataListEpisode.getValue());
				dataLists.add(dataListEpisode.getKey());
			}
		}
		assertEquals(4, dataLists.size());
		assertEquals("1", dataLists.get(0));
		assertEquals("2", dataLists.get(1));
		assertEquals("3", dataLists.get(2));
		assertEquals("7", dataLists.get(3));
		assertEquals(4, maxEpisode.size());
		assertEquals("25", maxEpisode.get(0));
		assertEquals("24", maxEpisode.get(1));
		assertEquals("24", maxEpisode.get(2));
		assertEquals("1", maxEpisode.get(3));
	}

	@Test
	public void testGetAnimeIdSeasonsAndEpisodesMapSeasonsAndEpisodesNotFound() {
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(
				"<div class=\"media__post__original-title\"> test title </div>");
		assertNotNull(animeIdSeasonsAndEpisodesMap);
		assertEquals(0, animeIdSeasonsAndEpisodesMap.size());
	}

	@Test
	public void testGetFirstEpisodeInSeason() {
		String firstEpisodeInSeason = animediaHTMLParser.getFirstEpisodeInSeason(routinesIO.readFromResource(saoDataList1));
		assertEquals("1", firstEpisodeInSeason);
		String firstEpisodeInSeasonOva = animediaHTMLParser.getFirstEpisodeInSeason(routinesIO.readFromResource(saoDataList7));
		assertEquals("1", firstEpisodeInSeasonOva);
		firstEpisodeInSeasonOva = animediaHTMLParser.getFirstEpisodeInSeason("<span>ОВА из 1</span>");
		assertEquals(BaseConstants.FIRST_EPISODE, firstEpisodeInSeasonOva);
	}

	@Test
	public void testGetFirstEpisodeInSeasonFirstEpisodeNotFound() {
		assertNull(animediaHTMLParser.getFirstEpisodeInSeason(""));
	}

	@Test
	public void testGetEpisodesRangeNormal() {
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(routinesIO.readFromResource(saoDataList1));
		checkEpisodesRange(range, "25", "1", "25", 25);
	}

	@Test
	public void testGetEpisodesRangeOvaWithFirstEpisode() {
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(routinesIO.readFromResource(saoDataList7));
		checkEpisodesRange(range, "1", "1", "1", 1);
	}

	@Test
	public void testGetEpisodesRangeOvaWithoutFirstEpisode() {
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange("<span>ОВА из 2</span>\n<span>ОВА из 2</span>");
		checkEpisodesRange(range, "2", BaseConstants.FIRST_EPISODE, "2", 2);
		range = animediaHTMLParser.getEpisodesRange("<span>Серия 1 из xxx</span>");
		checkEpisodesRange(range, "1", BaseConstants.FIRST_EPISODE, "xxx", 1);
	}

	@Test
	public void testGetEpisodesRangeUndefinedMax() {
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange("<span>Серия 1 из xxx</span>");
		checkEpisodesRange(range, "1", BaseConstants.FIRST_EPISODE, "xxx", 1);
	}

	@Test
	public void testGetEpisodesRangeRangeIsNotFound() {
		Map<String, List<String>> episodesRange = animediaHTMLParser.getEpisodesRange("<a href=\"/anime/mastera-mecha-onlayn/1/1\" title=\"Серия");
		assertNotNull(episodesRange);
		assertEquals(0, episodesRange.size());
		episodesRange = animediaHTMLParser.getEpisodesRange("");
		assertNotNull(episodesRange);
		assertEquals(0, episodesRange.size());
	}

	@Test
	public void testGetEpisodesRangeForTrailer() {
		Map<String, List<String>> episodesRangeForDataListWithTrailer = animediaHTMLParser.getEpisodesRange(routinesIO.readFromResource(
				dataListWithTrailer));
		checkEpisodesRange(episodesRangeForDataListWithTrailer, BaseConstants.ZERO_EPISODE, BaseConstants.ZERO_EPISODE, BaseConstants.ZERO_EPISODE, 1);
	}

	@Test
	public void testGetEpisodesRangeWithJoinedEpisodes() {
		Map<String, List<String>> episodesRangeWithJoinedEpisodes = animediaHTMLParser.getEpisodesRange(routinesIO.readFromResource(onePieceDataList2));
		checkEpisodesRange(episodesRangeWithJoinedEpisodes, "351", "176", "175", 171);
		assertEquals(5,
				Stream.of(episodesRangeWithJoinedEpisodes)
						.flatMap(x -> x.entrySet()
								.stream())
						.flatMap(x -> x.getValue()
								.stream())
						.filter(x -> x.matches("\\d{1,3}-\\d{1,3}"))
						.count());
	}

	@Test
	public void testGetOriginalTitle() {
		String originalTitle = animediaHTMLParser.getOriginalTitle(routinesIO.readFromResource(saoHtml));
		assertEquals("Sword Art Online", originalTitle);
	}

	@Test
	public void testGetOriginalTitleOriginalTitleIsNotFound() {
		assertNull(animediaHTMLParser.getOriginalTitle(""));
	}

	@Test
	public void testGetCurrentlyUpdatedTitlesList() {
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = animediaHTMLParser.getCurrentlyUpdatedTitlesList(routinesIO.readFromResource(
				pageWithCurrentlyAddedEpisodes));
		assertNotNull(currentlyUpdatedTitlesList);
		assertEquals(10, currentlyUpdatedTitlesList.size());
		List<AnimediaMALTitleReferences> currentlyAddedEpisodesListForCheck = getCurrentlyAddedEpisodesListForCheck();
		for (int i = 0; i < currentlyAddedEpisodesListForCheck.size(); i++) {
			assertEquals(currentlyUpdatedTitlesList.get(i)
							.getUrl(),
					currentlyAddedEpisodesListForCheck.get(i)
							.getUrl());
			assertEquals(currentlyUpdatedTitlesList.get(i)
							.getDataList(),
					currentlyAddedEpisodesListForCheck.get(i)
							.getDataList());
			assertEquals(currentlyUpdatedTitlesList.get(i)
							.getCurrentMax(),
					currentlyAddedEpisodesListForCheck.get(i)
							.getCurrentMax());
		}
	}

	@Test
	public void testGetCurrentlyUpdatedTitlesListIsNotFound() {
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = animediaHTMLParser.getCurrentlyUpdatedTitlesList("");
		assertNotNull(currentlyUpdatedTitlesList);
		assertEquals(0, currentlyUpdatedTitlesList.size());
	}

	private void checkEpisodesRange(Map<String, List<String>> range, String currentMax, String firstEpisode, String maxConcretizedEpisodeOnAnimedia,
			int rangeSize) {
		for (Map.Entry<String, List<String>> listEntry : range.entrySet()) {
			assertEquals(rangeSize,
					listEntry.getValue()
							.size());
			assertEquals(firstEpisode,
					listEntry.getValue()
							.get(0));
			assertEquals(currentMax,
					listEntry.getValue()
							.get(rangeSize - 1));
			assertEquals(maxConcretizedEpisodeOnAnimedia, listEntry.getKey());
		}
	}

	private List<AnimediaMALTitleReferences> getCurrentlyAddedEpisodesListForCheck() {
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = new ArrayList<>();
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/darovannyj")
				.dataList("1")
				.currentMax("9")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/bem")
				.dataList("1")
				.currentMax("7")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/dose-lorda-el-melloya-ii")
				.dataList("1")
				.currentMax("10")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/otvergnutyj-svyaschennyj-zver")
				.dataList("1")
				.currentMax("11")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/klinok-rassekayuschij-demonov")
				.dataList("1")
				.currentMax("23")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/molodaya-nevesta-gospodina-nobunagi")
				.dataList("")
				.currentMax("")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/arifureta-silnejshij-remeslennik-v-mire")
				.dataList("1")
				.currentMax("9")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/skazka-o-hvoste-fei-TV1")
				.dataList("3")
				.currentMax("325")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/one-piece-van-pis-tv")
				.dataList("5")
				.currentMax("901")
				.build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder()
				.url("/anime/mag-obmanschik-iz-drugogo-mira")
				.dataList("1")
				.currentMax("9")
				.build());
		return currentlyUpdatedTitlesList;
	}
}