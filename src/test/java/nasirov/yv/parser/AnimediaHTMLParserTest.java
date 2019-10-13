package nasirov.yv.parser;

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
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

/**
 * Created by nasirov.yv
 */

public class AnimediaHTMLParserTest extends AbstractTest {

	@Value("classpath:animedia/onePiece/onePiece2.txt")
	private Resource onePieceDataList2;

	@Value("classpath:animedia/announcements/dataListWithTrailer.txt")
	private Resource dataListWithTrailer;

	@Autowired
	private AnimediaHTMLParser animediaHTMLParser;

	@Test
	public void testGetAnimeIdSeasonsAndEpisodesMap() {
		HttpResponse multiSeasonsHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value());
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(multiSeasonsHtmlResponse);
		assertNotNull(animeIdSeasonsAndEpisodesMap);
		List<String> dataLists = new ArrayList<>();
		List<String> maxEpisode = new ArrayList<>();
		for (Map.Entry<String, Map<String, String>> entry : animeIdSeasonsAndEpisodesMap.entrySet()) {
			assertEquals(SAO_ID, entry.getKey());
			for (Map.Entry<String, String> dataListEpisode : entry.getValue().entrySet()) {
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

	@Test(expected = NullPointerException.class)
	public void testGetAnimeIdSeasonsAndEpisodesMapHttpResponseIsNull() {
		assertNotNull(animediaHTMLParser.getAnimeIdDataListsAndMaxEpisodesMap(null));
	}

	@Test
	public void testGetAnimeIdSeasonsAndEpisodesMapSeasonsAndEpisodesNotFound() {
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser
				.getAnimeIdDataListsAndMaxEpisodesMap(new HttpResponse("<div class=\"media__post__original-title\"> test title </div>", HttpStatus.OK.value()));
		assertNotNull(animeIdSeasonsAndEpisodesMap);
		assertEquals(0, animeIdSeasonsAndEpisodesMap.size());
	}

	@Test
	public void testGetFirstEpisodeInSeason() {
		HttpResponse firstDataListHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value());
		String firstEpisodeInSeason = animediaHTMLParser.getFirstEpisodeInSeason(firstDataListHtmlResponse);
		assertEquals("1", firstEpisodeInSeason);
		HttpResponse responseWithOVA = new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value());
		String firstEpisodeInSeasonOva = animediaHTMLParser.getFirstEpisodeInSeason(responseWithOVA);
		assertEquals("1", firstEpisodeInSeasonOva);
		HttpResponse responseWithoutFirstEpisode = new HttpResponse("<span>ОВА из 1</span>", HttpStatus.OK.value());
		firstEpisodeInSeasonOva = animediaHTMLParser.getFirstEpisodeInSeason(responseWithoutFirstEpisode);
		assertEquals(BaseConstants.FIRST_EPISODE, firstEpisodeInSeasonOva);
	}

	@Test(expected = NullPointerException.class)
	public void testGetFirstEpisodeInSeasonHttpResponseIsNull() {
		assertNull(animediaHTMLParser.getFirstEpisodeInSeason(null));
	}

	@Test
	public void testGetFirstEpisodeInSeasonFirstEpisodeNotFound() {
		assertNull(animediaHTMLParser.getFirstEpisodeInSeason(new HttpResponse("", HttpStatus.OK.value())));
	}

	@Test
	public void testGetEpisodesRangeNormal() {
		HttpResponse firstDataListHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(firstDataListHtmlResponse);
		checkEpisodesRange(range, "25", "1", "25", 25);
	}

	@Test
	public void testGetEpisodesRangeOvaWithFirstEpisode() {
		HttpResponse responseWithOVA = new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(responseWithOVA);
		checkEpisodesRange(range, "1", "1", "1", 1);
	}

	@Test
	public void testGetEpisodesRangeOvaWithoutFirstEpisode() {
		HttpResponse responseWithoutFirstEpisode = new HttpResponse("<span>ОВА из 2</span>\n<span>ОВА из 2</span>", HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(responseWithoutFirstEpisode);
		checkEpisodesRange(range, "2", BaseConstants.FIRST_EPISODE, "2", 2);
		HttpResponse responseWithUndefinedMax = new HttpResponse("<span>Серия 1 из xxx</span>", HttpStatus.OK.value());
		range = animediaHTMLParser.getEpisodesRange(responseWithUndefinedMax);
		checkEpisodesRange(range, "1", BaseConstants.FIRST_EPISODE, "xxx", 1);
	}

	@Test
	public void testGetEpisodesRangeUndefinedMax() {
		HttpResponse responseWithUndefinedMax = new HttpResponse("<span>Серия 1 из xxx</span>", HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(responseWithUndefinedMax);
		checkEpisodesRange(range, "1", BaseConstants.FIRST_EPISODE, "xxx", 1);
	}

	@Test(expected = NullPointerException.class)
	public void testGetEpisodesRangeHttpResponseIsNull() {
		assertNull(animediaHTMLParser.getEpisodesRange(null));
	}

	@Test
	public void testGetEpisodesRangeRangeIsNotFound() {
		Map<String, List<String>> episodesRange = animediaHTMLParser
				.getEpisodesRange(new HttpResponse("<a href=\"/anime/mastera-mecha-onlayn/1/1\" title=\"Серия", HttpStatus.OK.value()));
		assertNotNull(episodesRange);
		assertEquals(0, episodesRange.size());
		episodesRange = animediaHTMLParser.getEpisodesRange(new HttpResponse("", HttpStatus.OK.value()));
		assertNotNull(episodesRange);
		assertEquals(0, episodesRange.size());
	}

	@Test
	public void testGetEpisodesRangeForTrailer() {
		HttpResponse dataListWithTrailer1 = new HttpResponse(RoutinesIO.readFromResource(dataListWithTrailer), HttpStatus.OK.value());
		Map<String, List<String>> episodesRangeForDataListWithTrailer = animediaHTMLParser.getEpisodesRange(dataListWithTrailer1);
		checkEpisodesRange(episodesRangeForDataListWithTrailer, BaseConstants.ZERO_EPISODE, BaseConstants.ZERO_EPISODE, BaseConstants.ZERO_EPISODE,
				1);
	}

	@Test
	public void testGetEpisodesRangeWithJoinedEpisodes() {
		HttpResponse dataListWithTrailer1 = new HttpResponse(RoutinesIO.readFromResource(onePieceDataList2), HttpStatus.OK.value());
		Map<String, List<String>> episodesRangeWithJoinedEpisodes = animediaHTMLParser.getEpisodesRange(dataListWithTrailer1);
		checkEpisodesRange(episodesRangeWithJoinedEpisodes, "351", "176", "175", 171);
		assertEquals(5,
				Stream.of(episodesRangeWithJoinedEpisodes).flatMap(x -> x.entrySet().stream()).flatMap(x -> x.getValue().stream())
						.filter(x -> x.matches("\\d{1,3}-\\d{1,3}")).count());
	}

	@Test
	public void testGetOriginalTitle() {
		HttpResponse html = new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value());
		String originalTitle = animediaHTMLParser.getOriginalTitle(html);
		assertEquals("Sword Art Online", originalTitle);
	}

	@Test(expected = NullPointerException.class)
	public void testGetOriginalTitleHttpResponseIsNull() {
		assertNull(animediaHTMLParser.getOriginalTitle(null));
	}

	@Test
	public void testGetOriginalTitleOriginalTitleIsNotFound() {
		assertNull(animediaHTMLParser.getOriginalTitle(new HttpResponse("", HttpStatus.OK.value())));
	}

	@Test
	public void testGetCurrentlyUpdatedTitlesList() {
		HttpResponse html = new HttpResponse(RoutinesIO.readFromResource(pageWithCurrentlyAddedEpisodes), HttpStatus.OK.value());
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = animediaHTMLParser.getCurrentlyUpdatedTitlesList(html);
		assertNotNull(currentlyUpdatedTitlesList);
		assertEquals(10, currentlyUpdatedTitlesList.size());
		List<AnimediaMALTitleReferences> currentlyAddedEpisodesListForCheck = getCurrentlyAddedEpisodesListForCheck();
		for (int i = 0; i < currentlyAddedEpisodesListForCheck.size(); i++) {
			assertEquals(currentlyUpdatedTitlesList.get(i).getUrl(), currentlyAddedEpisodesListForCheck.get(i).getUrl());
			assertEquals(currentlyUpdatedTitlesList.get(i).getDataList(), currentlyAddedEpisodesListForCheck.get(i).getDataList());
			assertEquals(currentlyUpdatedTitlesList.get(i).getCurrentMax(), currentlyAddedEpisodesListForCheck.get(i).getCurrentMax());
		}
	}

	@Test
	public void testGetCurrentlyUpdatedTitlesListIsNotFound() {
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = animediaHTMLParser
				.getCurrentlyUpdatedTitlesList(new HttpResponse("", HttpStatus.OK.value()));
		assertNotNull(currentlyUpdatedTitlesList);
		assertEquals(0, currentlyUpdatedTitlesList.size());
	}

	private void checkEpisodesRange(Map<String, List<String>> range, String currentMax, String firstEpisode, String maxConcretizedEpisodeOnAnimedia,
			int rangeSize) {
		for (Map.Entry<String, List<String>> listEntry : range.entrySet()) {
			assertEquals(rangeSize, listEntry.getValue().size());
			assertEquals(firstEpisode, listEntry.getValue().get(0));
			assertEquals(currentMax, listEntry.getValue().get(rangeSize - 1));
			assertEquals(maxConcretizedEpisodeOnAnimedia, listEntry.getKey());
		}
	}

	private List<AnimediaMALTitleReferences> getCurrentlyAddedEpisodesListForCheck() {
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = new ArrayList<>();
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/darovannyj").dataList("1").currentMax("9").build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/bem").dataList("1").currentMax("7").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/dose-lorda-el-melloya-ii").dataList("1").currentMax("10").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/otvergnutyj-svyaschennyj-zver").dataList("1").currentMax("11").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/klinok-rassekayuschij-demonov").dataList("1").currentMax("23").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/molodaya-nevesta-gospodina-nobunagi").dataList("").currentMax("").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/arifureta-silnejshij-remeslennik-v-mire").dataList("1").currentMax("9").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/skazka-o-hvoste-fei-TV1").dataList("3").currentMax("325").build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/one-piece-van-pis-tv").dataList("5").currentMax("901").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/mag-obmanschik-iz-drugogo-mira").dataList("1").currentMax("9").build());
		return currentlyUpdatedTitlesList;
	}
}