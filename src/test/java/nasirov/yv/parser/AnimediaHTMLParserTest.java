package nasirov.yv.parser;

import static nasirov.yv.data.enums.Constants.FIRST_EPISODE;
import static nasirov.yv.data.enums.Constants.ZERO_EPISODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {AppConfiguration.class, AnimediaHTMLParser.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AnimediaHTMLParserTest extends AbstractTest {

	@Autowired
	private AnimediaHTMLParser animediaHTMLParser;

	@Test
	public void testGetAnimeIdSeasonsAndEpisodesMap() throws Exception {
		HttpResponse multiSeasonsHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value());
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(multiSeasonsHtmlResponse);
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
	public void testGetAnimeIdSeasonsAndEpisodesMapHttpResponseIsNull() throws Exception {
		assertNotNull(animediaHTMLParser.getAnimeIdSeasonsAndEpisodesMap(null));
	}

	@Test
	public void testGetAnimeIdSeasonsAndEpisodesMapSeasonsAndEpisodesNotFound() throws Exception {
		Map<String, Map<String, String>> animeIdSeasonsAndEpisodesMap = animediaHTMLParser
				.getAnimeIdSeasonsAndEpisodesMap(new HttpResponse("<div class=\"media__post__original-title\"> test title </div>", HttpStatus.OK.value()));
		assertNotNull(animeIdSeasonsAndEpisodesMap);
		assertEquals(0, animeIdSeasonsAndEpisodesMap.size());
	}

	@Test
	public void testGetFirstEpisodeInSeason() throws Exception {
		HttpResponse firstDataListHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value());
		String firstEpisodeInSeason = animediaHTMLParser.getFirstEpisodeInSeason(firstDataListHtmlResponse);
		assertEquals("1", firstEpisodeInSeason);
		HttpResponse responseWithOVA = new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value());
		String firstEpisodeInSeasonOva = animediaHTMLParser.getFirstEpisodeInSeason(responseWithOVA);
		assertEquals("1", firstEpisodeInSeasonOva);
		HttpResponse responseWithoutFirstEpisode = new HttpResponse("<span>ОВА из 1</span>", HttpStatus.OK.value());
		firstEpisodeInSeasonOva = animediaHTMLParser.getFirstEpisodeInSeason(responseWithoutFirstEpisode);
		assertEquals(FIRST_EPISODE.getDescription(), firstEpisodeInSeasonOva);
	}

	@Test(expected = NullPointerException.class)
	public void testGetFirstEpisodeInSeasonHttpResponseIsNull() throws Exception {
		assertNull(animediaHTMLParser.getFirstEpisodeInSeason(null));
	}

	@Test
	public void testGetFirstEpisodeInSeasonFirstEpisodeNotFound() throws Exception {
		assertNull(animediaHTMLParser.getFirstEpisodeInSeason(new HttpResponse("", HttpStatus.OK.value())));
	}

	@Test
	public void testGetEpisodesRangeNormal() throws Exception {
		HttpResponse firstDataListHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(firstDataListHtmlResponse);
		checkEpisodesRange(range, "25", "1", "25", 25);
	}

	@Test
	public void testGetEpisodesRangeOvaWithFirstEpisode() throws Exception {
		HttpResponse responseWithOVA = new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(responseWithOVA);
		checkEpisodesRange(range, "1", "1", "1", 1);
	}

	@Test
	public void testGetEpisodesRangeOvaWithoutFirstEpisode() throws Exception {
		HttpResponse responseWithoutFirstEpisode = new HttpResponse("<span>ОВА из 2</span>\n<span>ОВА из 2</span>", HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(responseWithoutFirstEpisode);
		checkEpisodesRange(range, "2", FIRST_EPISODE.getDescription(), "2", 2);
		HttpResponse responseWithUndefinedMax = new HttpResponse("<span>Серия 1 из xxx</span>", HttpStatus.OK.value());
		range = animediaHTMLParser.getEpisodesRange(responseWithUndefinedMax);
		checkEpisodesRange(range, "1", FIRST_EPISODE.getDescription(), "xxx", 1);
	}

	@Test
	public void testGetEpisodesRangeUndefinedMax() throws Exception {
		HttpResponse responseWithUndefinedMax = new HttpResponse("<span>Серия 1 из xxx</span>", HttpStatus.OK.value());
		Map<String, List<String>> range = animediaHTMLParser.getEpisodesRange(responseWithUndefinedMax);
		checkEpisodesRange(range, "1", FIRST_EPISODE.getDescription(), "xxx", 1);
	}

	@Test(expected = NullPointerException.class)
	public void testGetEpisodesRangeHttpResponseIsNull() throws Exception {
		assertNull(animediaHTMLParser.getEpisodesRange(null));
	}

	@Test
	public void testGetEpisodesRangeRangeIsNotFound() throws Exception {
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
		checkEpisodesRange(episodesRangeForDataListWithTrailer,
				ZERO_EPISODE.getDescription(),
				ZERO_EPISODE.getDescription(),
				ZERO_EPISODE.getDescription(),
				1);
	}

	@Test
	public void testGetOriginalTitle() throws Exception {
		HttpResponse html = new HttpResponse(RoutinesIO.readFromResource(saoHtml), HttpStatus.OK.value());
		String originalTitle = animediaHTMLParser.getOriginalTitle(html);
		assertEquals("Sword Art Online", originalTitle);
	}

	@Test(expected = NullPointerException.class)
	public void testGetOriginalTitleHttpResponseIsNull() throws Exception {
		assertNull(animediaHTMLParser.getOriginalTitle(null));
	}

	@Test
	public void testGetOriginalTitleOriginalTitleIsNotFound() throws Exception {
		assertNull(animediaHTMLParser.getOriginalTitle(new HttpResponse("", HttpStatus.OK.value())));
	}

	@Test
	public void testGetCurrentlyUpdatedTitlesList() throws Exception {
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

	@Test(expected = NullPointerException.class)
	public void testGetCurrentlyUpdatedTitlesListHttpResponseIsNull() throws Exception {
		assertNull(animediaHTMLParser.getCurrentlyUpdatedTitlesList(null));
	}

	@Test
	public void testGetCurrentlyUpdatedTitlesListIsNotFound() throws Exception {
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
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/pyat-nevest").dataList("1").currentMax("3").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove").dataList("1").currentMax("3")
						.build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/yarost-bahamuta-druzya-iz-manarii").dataList("1").currentMax("2").build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/chyornyj-klever").dataList("1").currentMax("68").build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/mastera-mecha-onlayn").dataList("3").currentMax("16").build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/domekano").dataList("1").currentMax("3").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/o-moyom-pererozhdenii-v-sliz").dataList("1").currentMax("17").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/boruto-novoe-pokolenie-naruto").dataList("1").currentMax("91").build());
		currentlyUpdatedTitlesList
				.add(AnimediaMALTitleReferences.builder().url("/anime/mob-psiho-100-mob-psycho-100").dataList("2").currentMax("4").build());
		currentlyUpdatedTitlesList.add(AnimediaMALTitleReferences.builder().url("/anime/one-piece-van-pis-tv").dataList("5").currentMax("870").build());
		return currentlyUpdatedTitlesList;
	}
}