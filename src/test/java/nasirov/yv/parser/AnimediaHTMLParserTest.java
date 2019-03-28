package nasirov.yv.parser;

import static nasirov.yv.enums.Constants.FIRST_EPISODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
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
	public void testGetEpisodesRange() throws Exception {
		HttpResponse firstDataListHtmlResponse = new HttpResponse(RoutinesIO.readFromResource(saoDataList1), HttpStatus.OK.value());
		Map<String, List<String>> episodesRangeForFirstDataList = animediaHTMLParser.getEpisodesRange(firstDataListHtmlResponse);
		checkEpisodesRange(episodesRangeForFirstDataList, "25", "1", 25);
		HttpResponse responseWithOVA = new HttpResponse(RoutinesIO.readFromResource(saoDataList7), HttpStatus.OK.value());
		Map<String, List<String>> ovaRange = animediaHTMLParser.getEpisodesRange(responseWithOVA);
		checkEpisodesRange(ovaRange, "1", "1", 1);
		HttpResponse responseWithoutFirstEpisode = new HttpResponse("<span>ОВА из 1</span>", HttpStatus.OK.value());
		ovaRange = animediaHTMLParser.getEpisodesRange(responseWithoutFirstEpisode);
		checkEpisodesRange(ovaRange, "1", FIRST_EPISODE.getDescription(), 1);
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

	private void checkEpisodesRange(Map<String, List<String>> range, String maxEpisode, String firstEpisode, int rangeSize) {
		for (Map.Entry<String, List<String>> listEntry : range.entrySet()) {
			assertEquals(rangeSize, listEntry.getValue().size());
			assertEquals(firstEpisode, listEntry.getValue().get(0));
			assertEquals(maxEpisode, listEntry.getValue().get(rangeSize - 1));
		}
	}

	private List<AnimediaMALTitleReferences> getCurrentlyAddedEpisodesListForCheck() {
		List<AnimediaMALTitleReferences> currentlyUpdatedTitlesList = new ArrayList<>();
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/pyat-nevest", "1", "", "", "", "", "3", "", "", ""));
		currentlyUpdatedTitlesList
				.add(new AnimediaMALTitleReferences("/anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove", "1", "", "", "", "", "3", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/yarost-bahamuta-druzya-iz-manarii", "1", "", "", "", "", "2", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/chyornyj-klever", "1", "", "", "", "", "68", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/mastera-mecha-onlayn", "3", "", "", "", "", "16", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/domekano", "1", "", "", "", "", "3", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/o-moyom-pererozhdenii-v-sliz", "1", "", "", "", "", "17", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/boruto-novoe-pokolenie-naruto", "1", "", "", "", "", "91", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/mob-psiho-100-mob-psycho-100", "2", "", "", "", "", "4", "", "", ""));
		currentlyUpdatedTitlesList.add(new AnimediaMALTitleReferences("/anime/one-piece-van-pis-tv", "5", "", "", "", "", "870", "", "", ""));
		return currentlyUpdatedTitlesList;
	}
}