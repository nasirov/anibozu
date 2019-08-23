package nasirov.yv.service;

import static nasirov.yv.TestUtils.getEpisodesRange;
import static nasirov.yv.data.mal.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.CacheConfiguration;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.util.RoutinesIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {CacheConfiguration.class, SeasonAndEpisodeChecker.class, AnimediaRequestParametersBuilder.class, AnimediaHTMLParser.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SeasonAndEpisodeCheckerTest extends AbstractTest {

	@Value("classpath:animedia/search/animediaSearchListSeveralTitlesMatchedForKeywords.json")
	private Resource animediaSearchListSeveralTitlesMatchedForKeywords;

	@Value("classpath:animedia/singleSeason/blackClover1.txt")
	private Resource blackCloverDataList1;

	@Value("classpath:animedia/singleSeason/anotherHtml.txt")
	private Resource anotherHtml;

	@Value("classpath:animedia/singleSeason/another1.txt")
	private Resource anotherDataList1;

	@Value("classpath:animedia/announcements/htmlWithAnnouncement.txt")
	private Resource htmlWithAnnouncement;

	private static final String ONE_PIECE_URL = "anime/one-piece-van-pis-tv";

	private static final String ONE_PIECE_NAME = "one piece";

	private static final String USERNAME = "testUsername";

	private static final String DATA_LIST_1 = "/1";

	private static final String DATA_LIST_1_EPISODE_1 = "/1/1";

	private static final String BLACK_CLOVER_URL = "anime/chyornyj-klever";

	private static final String BLACK_CLOVER_ID = "15341";

	private static final String BLACK_CLOVER_TITLE = "black clover";

	private static final String ANOTHER_URL = "anime/inayaAnother";

	private static final String ANOTHER_ID = "9392";

	private static final String ANOTHER_TITLE = "another";

	private static final String ONE_PUNCH_MAN_URL = "anime/vanpanchmen";

	private static final String ONE_PUNCH_MAN_ROAD_TO_HERO = "one punch man: road to hero";

	private static final String SAO_1_TITLE = "sword art online";

	private static final String SAO_1_URL = "anime/mastera-mecha-onlayn";

	private static final String NOT_FOUND_ON_ANIMEDIA_POSTER_URL = "notFoundOnAnimediaPosterUrl";

	private static final String BLACK_CLOVER_POSTER_URL = "blackCloverPosterUrl";

	private static final String ANOTHER_POSTER_URL = "anotherPosterUrl";

	private static final String ONE_PIECE_POSTER_URL = "onePiecePosterUrl";

	private static final String ONEPUNCHMAN_POSTER_URL = "onePunchManPosterUrl";

	private static final String SAO_1_POSTER_URL = "sao1PosterUrl";

	private static final String NOT_FOUND_ON_ANIMEDIA_TITLE = "notFoundOnAnimedia";

	private static final String ONE_PUNCH_MAN_SPECIALS = "one punch man specials";

	private static final String TAMAYURA = "tamayura";

	private static final String TAMAYURA_URL = "anime/tamayura";

	private static final String TAMAYURA_POSTER_URL = "tamayuraPosterUrl";


	@MockBean
	private HttpCaller httpCaller;

	@MockBean
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;

	@MockBean
	private ReferencesManager referencesManager;

	private List<UserMALTitleInfo> notFoundOnAnimediaRepoMock;

	@Autowired
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;

	@Before
	public void setUp() {
		super.setUp();
		notFoundOnAnimediaRepoMock = new ArrayList<>();
		doAnswer(answer -> {
			notFoundOnAnimediaRepoMock.add(answer.getArgument(0));
			return (answer.getArgument(0));
		}).when(notFoundAnimeOnAnimediaRepository).saveAndFlush(any(UserMALTitleInfo.class));
		doAnswer(answer -> notFoundOnAnimediaRepoMock.stream().filter(list -> String.valueOf(list.getTitle()).equals(answer.getArgument(0))).count() > 0)
				.when(notFoundAnimeOnAnimediaRepository).exitsByTitle(anyString());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(blackCloverHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + BLACK_CLOVER_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(blackCloverDataList1), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + BLACK_CLOVER_ID + DATA_LIST_1 + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(anotherHtml), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + ANOTHER_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(RoutinesIO.readFromResource(anotherDataList1), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaEpisodesList + ANOTHER_ID + DATA_LIST_1 + animediaEpisodesListPostfix), eq(HttpMethod.GET), anyMap());
	}

	@Test
	public void getMatchedAnimeNewEpisodesAvailable() throws Exception {
		Integer blackCloverNumWatchingEpisodes = 0;
		Integer onePieceNumWatchingEpisodes = 0;
		Integer anotherNumWatchingEpisodes = 0;
		Integer onePunchManRoadToHeroNumWatchedEpisodes = 0;
		Integer onePunchManSpecialsNumWatchedEpisodes = 0;
		Integer sao1NumWatchedEpisodes = 0;
		Integer tamayuraNumWatchedEpisodes = 1;
		checkMatchedResult(blackCloverNumWatchingEpisodes,
				onePieceNumWatchingEpisodes,
				anotherNumWatchingEpisodes,
				onePunchManRoadToHeroNumWatchedEpisodes,
				onePunchManSpecialsNumWatchedEpisodes,
				sao1NumWatchedEpisodes,
				tamayuraNumWatchedEpisodes,
				true);
	}

	@Test
	public void getMatchedAnimeNewEpisodesNotAvailable() throws Exception {
		Integer blackCloverNumWatchingEpisodes = 69;
		Integer onePieceNumWatchingEpisodes = 870;
		Integer anotherNumWatchingEpisodes = 12;
		Integer onePunchManRoadToHeroNumWatchedEpisodes = 1;
		Integer onePunchManSpecialsNumWatchedEpisodes = 6;
		Integer sao1NumWatchedEpisodes = 12;
		Integer tamayuraNumWatchedEpisodes = 4;
		checkMatchedResult(blackCloverNumWatchingEpisodes,
				onePieceNumWatchingEpisodes,
				anotherNumWatchingEpisodes,
				onePunchManRoadToHeroNumWatchedEpisodes,
				onePunchManSpecialsNumWatchedEpisodes,
				sao1NumWatchedEpisodes,
				tamayuraNumWatchedEpisodes,
				false);
	}

	@Test
	public void getMatchedAnimeMoreThanOneMatchesInAnimediaSearchList() {
		Integer blackCloverNumWatchingEpisodes = 0;
		Integer onePieceNumWatchingEpisodes = 0;
		Integer anotherNumWatchingEpisodes = 0;
		Integer onePunchManRoadToHeroNumWatchedEpisodes = 0;
		Integer onePunchManSpecialsNumWatchedEpisodes = 0;
		Integer sao1NumWatchedEpisodes = 0;
		Integer tamayuraNumWatchedEpisodes = 0;
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(blackCloverNumWatchingEpisodes,
				onePieceNumWatchingEpisodes,
				anotherNumWatchingEpisodes,
				onePunchManRoadToHeroNumWatchedEpisodes,
				onePunchManSpecialsNumWatchedEpisodes,
				sao1NumWatchedEpisodes,
				tamayuraNumWatchedEpisodes);
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		AnimediaTitleSearchInfo singleWithEqualsKeywords = new AnimediaTitleSearchInfo("another smth to test",
				ANOTHER_TITLE,
				"anime/another-smth-to-test",
				"anotherSmthToTestUrl");
		animediaSearchList.add(singleWithEqualsKeywords);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker
				.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertNotNull(matchedAnime);
		assertEquals((watchingTitles.size() - 2), matchedAnime.size());
		AnimediaMALTitleReferences another = AnimediaMALTitleReferences.builder().url(ANOTHER_URL).dataList("1").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(ANOTHER_TITLE).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("12").currentMax("12")
				.posterUrl(myAnimeListStaticContentUrl + ANOTHER_POSTER_URL).build();
		String anotherEpisodeNumberForWatch = String.valueOf(anotherNumWatchingEpisodes + 1);
		String anotherFinalUrl = animediaOnlineTv + another.getUrl() + "/" + another.getDataList() + "/" + String.valueOf(anotherNumWatchingEpisodes +
				1);
		assertEquals(0,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(another.getTitleOnMAL()) && set.getUrl().equals(another.getUrl()) && set.getDataList()
								.equals(another.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia().equals(another.getMinConcretizedEpisodeOnAnimedia()) &&
								set
								.getMaxConcretizedEpisodeOnAnimedia().equals(another.getMaxConcretizedEpisodeOnAnimedia()) && set.getCurrentMax()
								.equals(another.getCurrentMax()) && set.getEpisodeNumberForWatch().equals(anotherEpisodeNumberForWatch) && set.getFinalUrl()
								.equals(anotherFinalUrl) && set.getPosterUrl().equals(another.getPosterUrl())).count());
		assertEquals(1, notFoundOnAnimediaRepoMock.size());
		assertEquals(NOT_FOUND_ON_ANIMEDIA_TITLE, notFoundOnAnimediaRepoMock.get(0).getTitle());
	}

	@Test
	public void getMatchedAnimeMoreThanOneMatchesInMultiSeasonsReferences() {
		Integer blackCloverNumWatchingEpisodes = 0;
		Integer onePieceNumWatchingEpisodes = 800;
		Integer anotherNumWatchingEpisodes = 0;
		Integer onePunchManRoadToHeroNumWatchedEpisodes = 0;
		Integer onePunchManSpecialsNumWatchedEpisodes = 0;
		Integer sao1NumWatchedEpisodes = 0;
		Integer tamayuraNumWatchedEpisodes = 3;
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(blackCloverNumWatchingEpisodes,
				onePieceNumWatchingEpisodes,
				anotherNumWatchingEpisodes,
				onePunchManRoadToHeroNumWatchedEpisodes,
				onePunchManSpecialsNumWatchedEpisodes,
				sao1NumWatchedEpisodes,
				tamayuraNumWatchedEpisodes);
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker
				.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertNotNull(matchedAnime);
		assertEquals(7, matchedAnime.size());
		AnimediaMALTitleReferences onePiece = multiSeasonsReferencesList.stream()
				.filter(ref -> ref.getTitleOnMAL().equals(ONE_PIECE_NAME) && ref.getDataList().equals("5")).findFirst().get();
		String onePieceEpisodeNumberForWatch = String.valueOf(onePieceNumWatchingEpisodes + 1);
		String onePieceFinalUrl =
				animediaOnlineTv + onePiece.getUrl() + "/" + onePiece.getDataList() + "/" + String.valueOf(onePieceNumWatchingEpisodes + 1);
		AnimediaMALTitleReferences tamayura = multiSeasonsReferencesList.stream()
				.filter(ref -> ref.getTitleOnMAL().equals(TAMAYURA) && Integer.parseInt(ref.getMinConcretizedEpisodeOnMAL()) <= (tamayuraNumWatchedEpisodes
						+ 1) && Integer.parseInt(ref.getMaxConcretizedEpisodeOnMAL()) >= (tamayuraNumWatchedEpisodes + 1)).findFirst().get();
		String tamayuraEpisodeNumberForWatch = String.valueOf(tamayuraNumWatchedEpisodes + 1);
		String tamayuraFinalUrl = animediaOnlineTv + tamayura.getUrl() + "/" + tamayura.getDataList() + "/" + tamayura.getFirstEpisode();
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(onePiece.getTitleOnMAL()) && set.getUrl().equals(onePiece.getUrl()) && set.getDataList()
								.equals(onePiece.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia().equals(onePiece.getMinConcretizedEpisodeOnAnimedia())
								&& set.getMaxConcretizedEpisodeOnAnimedia().equals(onePiece.getMaxConcretizedEpisodeOnAnimedia()) && set.getCurrentMax()
								.equals(onePiece.getCurrentMax()) && set.getEpisodeNumberForWatch().equals(onePieceEpisodeNumberForWatch) && set.getFinalUrl()
								.equals(onePieceFinalUrl) && set.getPosterUrl().equals(onePiece.getPosterUrl())).count());
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(tamayura.getTitleOnMAL()) && set.getUrl().equals(tamayura.getUrl()) && set.getDataList()
								.equals(tamayura.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia().equals(tamayura.getMinConcretizedEpisodeOnAnimedia())
								&& set.getMaxConcretizedEpisodeOnAnimedia().equals(tamayura.getMaxConcretizedEpisodeOnAnimedia()) && set
								.getMinConcretizedEpisodeOnMAL().equals(tamayura.getMinConcretizedEpisodeOnMAL()) && set.getMaxConcretizedEpisodeOnMAL()
								.equals(tamayura.getMaxConcretizedEpisodeOnMAL()) && set.getCurrentMax().equals(tamayura.getCurrentMax()) && set
								.getEpisodeNumberForWatch().equals(tamayuraEpisodeNumberForWatch) && set.getFinalUrl().equals(tamayuraFinalUrl) && set.getPosterUrl()
								.equals(tamayura.getPosterUrl())).count());
		assertEquals(1, notFoundOnAnimediaRepoMock.size());
		assertEquals(NOT_FOUND_ON_ANIMEDIA_TITLE, notFoundOnAnimediaRepoMock.get(0).getTitle());
	}

	@Test
	public void getMatchedAnimeUpdateMultiseasonsReferences() {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = new LinkedHashSet<>();
		AnimediaMALTitleReferences notUpdatedReference = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("1").firstEpisode("1")
				.titleOnMAL(ONE_PIECE_NAME).currentMax("10").build();
		multiSeasonsReferencesList.add(notUpdatedReference);
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo addedTitleInUserCachedPeriod = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				ONE_PIECE_NAME,
				0,
				myAnimeListStaticContentUrl + ONE_PIECE_POSTER_URL,
				"onePieceAnimeUrl");
		watchingTitles.add(addedTitleInUserCachedPeriod);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		doAnswer((x)->{notUpdatedReference.setEpisodesRange(getEpisodesRange("1","10"));return Void.TYPE;}).when(referencesManager).updateReferences
				(anySet());
		seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		verify(referencesManager, times(1)).updateReferences(anySet());
		assertEquals(addedTitleInUserCachedPeriod.getPosterUrl(), notUpdatedReference.getPosterUrl());
	}

	@Test
	public void getMatchedAnimeOngoingOnMALAnnouncementOnAnimedia() {
		String announcementUrl = "anime/zvyozdy-ansamblya";
		doReturn(new HttpResponse(RoutinesIO.readFromResource(htmlWithAnnouncement), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + announcementUrl), eq(HttpMethod.GET), anyMap());
		String titleOnMal = "ensemble stars!";
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = new LinkedHashSet<>();
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo addedTitleInUserCachedPeriod = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				titleOnMal,
				0,
				myAnimeListStaticContentUrl + "titleOnMal",
				titleOnMal + "AnimeUrl");
		watchingTitles.add(addedTitleInUserCachedPeriod);
		Set<AnimediaTitleSearchInfo> animediaSearchList = new LinkedHashSet<>();
		AnimediaTitleSearchInfo title = new AnimediaTitleSearchInfo("звезды ансамбля", titleOnMal, announcementUrl, "posterUrl");
		animediaSearchList.add(title);
		Set<AnimediaMALTitleReferences> matchedAnimeSet = seasonAndEpisodeChecker
				.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertEquals(1, matchedAnimeSet.size());
		AnimediaMALTitleReferences matchedAnime = matchedAnimeSet.stream().findAny().orElse(null);
		assertEquals(announcementUrl, matchedAnime.getUrl());
		assertEquals(BaseConstants.FIRST_DATA_LIST, matchedAnime.getDataList());
		assertEquals(titleOnMal, matchedAnime.getTitleOnMAL());
		assertEquals(BaseConstants.FIRST_EPISODE, matchedAnime.getFirstEpisode());
		assertEquals(BaseConstants.ZERO_EPISODE, matchedAnime.getCurrentMax());
		assertEquals(BaseConstants.ZERO_EPISODE, matchedAnime.getMinConcretizedEpisodeOnAnimedia());
		assertEquals(BaseConstants.ZERO_EPISODE, matchedAnime.getMaxConcretizedEpisodeOnAnimedia());
		assertEquals(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, matchedAnime.getFinalUrl());
		assertEquals(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE, matchedAnime.getEpisodeNumberForWatch());
		assertEquals(null, matchedAnime.getMinConcretizedEpisodeOnMAL());
		assertEquals(null, matchedAnime.getMaxConcretizedEpisodeOnMAL());
	}

	@Test
	public void getMatchedAnimeJoinedEpisodeIsPresent() {
		String rootUrl = "anime/someAnime";
		doReturn(new HttpResponse(RoutinesIO.readFromResource(htmlWithAnnouncement), HttpStatus.OK.value())).when(httpCaller)
				.call(eq(animediaOnlineTv + rootUrl), eq(HttpMethod.GET), anyMap());
		String titleOnMal = "some name";
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = new LinkedHashSet<>();
		AnimediaMALTitleReferences references = AnimediaMALTitleReferences.builder().url(rootUrl).dataList("1").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(titleOnMal).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("7").currentMax("7").posterUrl("nwm")
				.episodesRange(Arrays.asList("1-2","3","4-6","7")).build();
		multiSeasonsReferencesList.add(references);
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		int numOfWatchedEpisodes = 3;
		UserMALTitleInfo title = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				numOfWatchedEpisodes,
				titleOnMal,
				0,
				myAnimeListStaticContentUrl + "titleOnMal",
				titleOnMal + "AnimeUrl");
		watchingTitles.add(title);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker
				.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, new LinkedHashSet<>(), USERNAME);
		assertEquals(String.valueOf(numOfWatchedEpisodes+1), matchedAnime.stream().findFirst().get().getEpisodeNumberForWatch());
		assertEquals(animediaOnlineTv + rootUrl + "/" + "1/" + String.valueOf(numOfWatchedEpisodes+1), matchedAnime.stream().findFirst().get().getFinalUrl());
		numOfWatchedEpisodes = 4;
		title.setNumWatchedEpisodes(numOfWatchedEpisodes);
		matchedAnime = seasonAndEpisodeChecker
				.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, new LinkedHashSet<>(), USERNAME);
		assertEquals(String.valueOf(numOfWatchedEpisodes+1), matchedAnime.stream().findFirst().get().getEpisodeNumberForWatch());
		assertEquals(animediaOnlineTv + rootUrl + "/" + "1/" + numOfWatchedEpisodes, matchedAnime.stream().findFirst().get().getFinalUrl());
	}

	@Test
	public void updateMatchedReferences() throws Exception {
		Set<AnimediaMALTitleReferences> matchedReferences = getReferences();
		for (AnimediaMALTitleReferences references : matchedReferences) {
			if (references.getMinConcretizedEpisodeOnMAL() != null && references.getMaxConcretizedEpisodeOnMAL() != null) {
				continue;
			}
			int numWatchedEpisodes = Integer.parseInt(references.getFirstEpisode()) + 1;
			String dataList = references.getDataList();
			String firstEpisodeAndMin = references.getMinConcretizedEpisodeOnAnimedia();
			String max = references.getMaxConcretizedEpisodeOnAnimedia();
			String currentMax = references.getCurrentMax();
			String nextEpisodeForWatch = String.valueOf(numWatchedEpisodes + 1);
			Set<UserMALTitleInfo> watchingTitlesFresh = getWatchingTitles(numWatchedEpisodes,
					numWatchedEpisodes,
					numWatchedEpisodes,
					0,
					0,
					numWatchedEpisodes,
					0);
			AnimediaMALTitleReferences currentlyUpdatedReference = AnimediaMALTitleReferences.builder().url(references.getUrl()).dataList(dataList)
					.minConcretizedEpisodeOnAnimedia(firstEpisodeAndMin).titleOnMAL(references.getTitleOnMAL()).firstEpisode(firstEpisodeAndMin)
					.maxConcretizedEpisodeOnAnimedia(max).currentMax(currentMax).posterUrl(references.getPosterUrl()).build();
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesFresh, currentlyUpdatedReference, matchedReferences);
			assertEquals(1,
					matchedReferences.stream()
							.filter(set -> set.getTitleOnMAL().equals(references.getTitleOnMAL()) && set.getUrl().equals(references.getUrl()) && set.getDataList()
									.equals(dataList) && set.getMinConcretizedEpisodeOnAnimedia().equals(firstEpisodeAndMin) && set
									.getMaxConcretizedEpisodeOnAnimedia()
									.equals(max) && set.getCurrentMax().equals(currentMax) && set.getEpisodeNumberForWatch().equals(nextEpisodeForWatch) && set
									.getFinalUrl().equals(animediaOnlineTv + references.getUrl() + "/" + dataList + "/" + nextEpisodeForWatch)).count());
		}
	}

	@Test
	public void updateEpisodeNumberForWatchAndFinalUrlForMultiSeasonsReferencesWithEqualsTitle() {
//		7.2 https://online.animedia.tv/anime/one-piece-van-pis-tv/2/176 one piece 176-351
//		7.3 https://online.animedia.tv/anime/one-piece-van-pis-tv/3/352 one piece 352-527
//		7.4 https://online.animedia.tv/anime/one-piece-van-pis-tv/4/528 one piece 528-700
//		7.5 https://online.animedia.tv/anime/one-piece-van-pis-tv/5/701 one piece 701-xxx
		Integer numWatchedEpisodes = 175;
		String episodeNumberForWatch = String.valueOf(numWatchedEpisodes + 1);
		String posterUrl = myAnimeListStaticContentUrl + ONE_PIECE_POSTER_URL;
		UserMALTitleInfo multi = new UserMALTitleInfo(0, WATCHING.getCode(), numWatchedEpisodes, ONE_PIECE_NAME, 0, posterUrl, "onePieceAnimeUrl");
		Set<UserMALTitleInfo> updatedWatchingTitle = new LinkedHashSet<>();
		updatedWatchingTitle.add(multi);
		Set<AnimediaMALTitleReferences> matchedReferences = new LinkedHashSet<>();
		AnimediaMALTitleReferences onePiece1 = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("1").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(ONE_PIECE_NAME).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175").posterUrl(posterUrl)
				.episodeNumberForWatch("1").finalUrl(animediaOnlineTv + ONE_PIECE_URL + DATA_LIST_1_EPISODE_1).build();
		matchedReferences.add(onePiece1);
		Set<AnimediaMALTitleReferences> allReferences = getReferences();
		AnimediaMALTitleReferences onePiece2 = allReferences.stream().filter(ref -> ref.getDataList().equals("2")).findFirst().get();
		doReturn(allReferences).when(referencesManager).getMultiSeasonsReferences();
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
		assertEquals(1, matchedReferences.size());
		String temp = episodeNumberForWatch;
		assertEquals(1,
				matchedReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(onePiece2.getTitleOnMAL()) && ref.getDataList().equals(onePiece2.getDataList()) && ref
								.getFirstEpisode().equals(onePiece2.getFirstEpisode()) && ref.getUrl().equals(onePiece2.getUrl()) && ref
								.getMinConcretizedEpisodeOnAnimedia().equals(onePiece2.getMinConcretizedEpisodeOnAnimedia()) && ref
								.getMaxConcretizedEpisodeOnAnimedia().equals(onePiece2.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
								.equals(onePiece2.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(temp) && ref.getFinalUrl()
								.equals(animediaOnlineTv + onePiece2.getUrl() + "/" + onePiece2.getDataList() + "/" + temp)).count());
		numWatchedEpisodes = 870;
		multi.setNumWatchedEpisodes(numWatchedEpisodes);
		AnimediaMALTitleReferences onePiece5 = allReferences.stream().filter(ref -> ref.getDataList().equals("5")).findFirst().get();
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
		assertEquals(1, matchedReferences.size());
		assertEquals(1,
				matchedReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(onePiece5.getTitleOnMAL()) && ref.getDataList().equals(onePiece5.getDataList()) && ref
								.getFirstEpisode().equals(onePiece5.getFirstEpisode()) && ref.getUrl().equals(onePiece5.getUrl()) && ref
								.getMinConcretizedEpisodeOnAnimedia().equals(onePiece5.getMinConcretizedEpisodeOnAnimedia()) && ref
								.getMaxConcretizedEpisodeOnAnimedia().equals(onePiece5.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
								.equals(onePiece5.getCurrentMax()) && ref.getEpisodeNumberForWatch()
								.equals(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) && ref.getFinalUrl()
								.equals(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE)).count());
		numWatchedEpisodes = 420;
		multi.setNumWatchedEpisodes(numWatchedEpisodes);
		episodeNumberForWatch = String.valueOf(numWatchedEpisodes + 1);
		AnimediaMALTitleReferences onePiece3 = allReferences.stream().filter(ref -> ref.getDataList().equals("3")).findFirst().get();
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
		assertEquals(1, matchedReferences.size());
		String temp2 = episodeNumberForWatch;
		assertEquals(1,
				matchedReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(onePiece3.getTitleOnMAL()) && ref.getDataList().equals(onePiece3.getDataList()) && ref
								.getFirstEpisode().equals(onePiece3.getFirstEpisode()) && ref.getUrl().equals(onePiece3.getUrl()) && ref
								.getMinConcretizedEpisodeOnAnimedia().equals(onePiece3.getMinConcretizedEpisodeOnAnimedia()) && ref
								.getMaxConcretizedEpisodeOnAnimedia().equals(onePiece3.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
								.equals(onePiece3.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(temp2) && ref.getFinalUrl()
								.equals(animediaOnlineTv + onePiece3.getUrl() + "/" + onePiece3.getDataList() + "/" + temp2)).count());
	}

	@Test
	public void updateEpisodeNumberForWatchAndFinalUrlForConcretizedMultiSeasonsReferencesWithEqualsTitleOnSameDataListAndUnionSeveralEpisodesInOne() {
//		1-2 https://online.animedia.tv/anime/tamayura/2/1 Tamayura  1-1
//		3-4 https://online.animedia.tv/anime/tamayura/2/2 Tamayura  2-2
		Integer numWatchedEpisodes = 2;
		String episodeNumberForWatch = String.valueOf(numWatchedEpisodes + 1);
		String posterUrl = myAnimeListStaticContentUrl + TAMAYURA_POSTER_URL;
		UserMALTitleInfo tamayura = new UserMALTitleInfo(0, WATCHING.getCode(), numWatchedEpisodes, TAMAYURA, 4, posterUrl, "tamayuraAnimeUrl");
		Set<UserMALTitleInfo> updatedWatchingTitle = new LinkedHashSet<>();
		updatedWatchingTitle.add(tamayura);
		Set<AnimediaMALTitleReferences> matchedReferences = new LinkedHashSet<>();
		AnimediaMALTitleReferences tamayura1_2 = AnimediaMALTitleReferences.builder().url(TAMAYURA_URL).dataList("2").minConcretizedEpisodeOnAnimedia
				("1")
				.titleOnMAL(TAMAYURA).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("1").currentMax("1").posterUrl(posterUrl).episodeNumberForWatch("1")
				.finalUrl(animediaOnlineTv + TAMAYURA_URL + "/2/1").minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("2").build();
		AnimediaMALTitleReferences tamayura3_4 = AnimediaMALTitleReferences.builder().url(TAMAYURA_URL).dataList("2").minConcretizedEpisodeOnAnimedia
				("2")
				.titleOnMAL(TAMAYURA).firstEpisode("2").maxConcretizedEpisodeOnAnimedia("2").currentMax("2").posterUrl(posterUrl)
				.minConcretizedEpisodeOnMAL("3").maxConcretizedEpisodeOnMAL("4").build();
		matchedReferences.add(tamayura1_2);
		Set<AnimediaMALTitleReferences> allReferences = new LinkedHashSet<>();
		allReferences.add(tamayura1_2);
		allReferences.add(tamayura3_4);
		doReturn(allReferences).when(referencesManager).getMultiSeasonsReferences();
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
		assertEquals(1, matchedReferences.size());
		assertEquals(1,
				matchedReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(tamayura3_4.getTitleOnMAL()) && ref.getDataList().equals(tamayura3_4.getDataList()) && ref
								.getFirstEpisode().equals(tamayura3_4.getFirstEpisode()) && ref.getUrl().equals(tamayura3_4.getUrl()) && ref
								.getMinConcretizedEpisodeOnAnimedia().equals(tamayura3_4.getMinConcretizedEpisodeOnAnimedia()) && ref
								.getMaxConcretizedEpisodeOnAnimedia().equals(tamayura3_4.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
								.equals(tamayura3_4.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(episodeNumberForWatch) && ref.getFinalUrl()
								.equals(animediaOnlineTv + tamayura3_4.getUrl() + "/" + tamayura3_4.getDataList() + "/" + tamayura3_4.getCurrentMax()) && ref
								.getMinConcretizedEpisodeOnMAL().equals(tamayura3_4.getMinConcretizedEpisodeOnMAL()) && ref.getMaxConcretizedEpisodeOnMAL()
								.equals(tamayura3_4.getMaxConcretizedEpisodeOnMAL())).count());
		matchedReferences.clear();
		matchedReferences.add(new AnimediaMALTitleReferences(tamayura3_4));
		allReferences.clear();
		tamayura1_2.setEpisodeNumberForWatch(null);
		tamayura1_2.setFinalUrl(null);
		tamayura3_4.setEpisodeNumberForWatch(null);
		tamayura3_4.setFinalUrl(null);
		allReferences.add(tamayura1_2);
		allReferences.add(tamayura3_4);
		numWatchedEpisodes = 1;
		String episodeNumberForWatch2 = String.valueOf(numWatchedEpisodes + 1);
		tamayura.setNumWatchedEpisodes(numWatchedEpisodes);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
		assertEquals(1, matchedReferences.size());
		assertEquals(1,
				matchedReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(tamayura1_2.getTitleOnMAL()) && ref.getDataList().equals(tamayura1_2.getDataList()) && ref
								.getFirstEpisode().equals(tamayura1_2.getFirstEpisode()) && ref.getUrl().equals(tamayura1_2.getUrl()) && ref
								.getMinConcretizedEpisodeOnAnimedia().equals(tamayura1_2.getMinConcretizedEpisodeOnAnimedia()) && ref
								.getMaxConcretizedEpisodeOnAnimedia().equals(tamayura1_2.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
								.equals(tamayura1_2.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(episodeNumberForWatch2) && ref.getFinalUrl()
								.equals(animediaOnlineTv + tamayura1_2.getUrl() + "/" + tamayura1_2.getDataList() + "/" + tamayura1_2.getCurrentMax()) && ref
								.getMinConcretizedEpisodeOnMAL().equals(tamayura1_2.getMinConcretizedEpisodeOnMAL()) && ref.getMaxConcretizedEpisodeOnMAL()
								.equals(tamayura1_2.getMaxConcretizedEpisodeOnMAL())).count());
		matchedReferences.clear();
		matchedReferences.add(new AnimediaMALTitleReferences(tamayura1_2));
		allReferences.clear();
		tamayura1_2.setEpisodeNumberForWatch(null);
		tamayura1_2.setFinalUrl(null);
		tamayura3_4.setEpisodeNumberForWatch(null);
		tamayura3_4.setFinalUrl(null);
		allReferences.add(tamayura1_2);
		allReferences.add(tamayura3_4);
		numWatchedEpisodes = 4;
		tamayura.setNumWatchedEpisodes(numWatchedEpisodes);
		seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
		assertEquals(1, matchedReferences.size());
		assertEquals(1,
				matchedReferences.stream()
						.filter(ref -> ref.getTitleOnMAL().equals(tamayura3_4.getTitleOnMAL()) && ref.getDataList().equals(tamayura3_4.getDataList()) && ref
								.getFirstEpisode().equals(tamayura3_4.getFirstEpisode()) && ref.getUrl().equals(tamayura3_4.getUrl()) && ref
								.getMinConcretizedEpisodeOnAnimedia().equals(tamayura3_4.getMinConcretizedEpisodeOnAnimedia()) && ref
								.getMaxConcretizedEpisodeOnAnimedia().equals(tamayura3_4.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
								.equals(tamayura3_4.getCurrentMax()) && ref.getEpisodeNumberForWatch()
								.equals(BaseConstants.EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) && ref.getFinalUrl()
								.equals(BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE) && ref.getMinConcretizedEpisodeOnMAL()
								.equals(tamayura3_4.getMinConcretizedEpisodeOnMAL()) && ref.getMaxConcretizedEpisodeOnMAL()
								.equals(tamayura3_4.getMaxConcretizedEpisodeOnMAL())).count());
	}

	@Test
	public void updateEpisodeNumberForWatchAndFinalUrlForConcretizedMultiSeasonsReferenceWithRangeEpisodesOnMalAndAnimedia() {
//		1-3 https://online.animedia.tv/anime/servamp/2/1 Servamp Specials 1-3
		String servampTitle = "servamp";
		String servampUrl = "anime/servamp";
		UserMALTitleInfo servamp = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				servampTitle,
				4,
				myAnimeListStaticContentUrl + "servampTitlePosterUrl",
				"servampAnimeUrl");
		String posterUrl = myAnimeListStaticContentUrl + "servampTitlePosterUrl";
		AnimediaMALTitleReferences servamp1_3 = AnimediaMALTitleReferences.builder().url(servampUrl).dataList("2").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(servampTitle).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("3").currentMax("3").posterUrl(posterUrl)
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("3").build();
		for (int i = 0; i < 4; i++) {
			Integer numWatchedEpisodes = i;
			String episodeNumberForWatch = String.valueOf(numWatchedEpisodes + 1);
			servamp.setNumWatchedEpisodes(numWatchedEpisodes);
			Set<UserMALTitleInfo> updatedWatchingTitle = new LinkedHashSet<>();
			updatedWatchingTitle.add(servamp);
			Set<AnimediaMALTitleReferences> matchedReferences = new LinkedHashSet<>();
			matchedReferences.add(servamp1_3);
			Set<AnimediaMALTitleReferences> allReferences = new LinkedHashSet<>();
			allReferences.add(servamp1_3);
			doReturn(allReferences).when(referencesManager).getMultiSeasonsReferences();
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
			String finalUrl = animediaOnlineTv + servamp1_3.getUrl() + "/" + servamp1_3.getDataList() + "/" + episodeNumberForWatch;
			if (i == 3) {
				episodeNumberForWatch = "";
				finalUrl = "";
			}
			String finalEpisodeNumberForWatchForStream = episodeNumberForWatch;
			String finalUrlForStream = finalUrl;
			assertEquals(1, matchedReferences.size());
			assertEquals(1,
					matchedReferences.stream()
							.filter(ref -> ref.getTitleOnMAL().equals(servamp1_3.getTitleOnMAL()) && ref.getDataList().equals(servamp1_3.getDataList()) && ref
									.getFirstEpisode().equals(servamp1_3.getFirstEpisode()) && ref.getUrl().equals(servamp1_3.getUrl()) && ref
									.getMinConcretizedEpisodeOnAnimedia().equals(servamp1_3.getMinConcretizedEpisodeOnAnimedia()) && ref
									.getMaxConcretizedEpisodeOnAnimedia().equals(servamp1_3.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
									.equals(servamp1_3.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(finalEpisodeNumberForWatchForStream) && ref
									.getFinalUrl().equals(finalUrlForStream) && ref.getMinConcretizedEpisodeOnMAL().equals(servamp1_3.getMinConcretizedEpisodeOnMAL())
									&& ref.getMaxConcretizedEpisodeOnMAL().equals(servamp1_3.getMaxConcretizedEpisodeOnMAL())).count());
		}
	}

	@Test
	public void updateEpisodeNumberForWatchAndFinalUrlForConcretizedMultiSeasonsReferenceWithSingleEpisodesOnMalAndAnimedia() {
		//1-1 https://online.animedia.tv/anime/vanpanchmen/7/7 One Punch Man: Road to Hero 7-7
		String onePunchManTitle = "one punch man: road to hero";
		String onePunchManUrl = "anime/vanpanchmen";
		String posterUrl = myAnimeListStaticContentUrl + ONEPUNCHMAN_POSTER_URL;
		int animeNumEpisodes = 1;
		UserMALTitleInfo onePunchMan = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				onePunchManTitle,
				animeNumEpisodes,
				posterUrl,
				"onePunchManAnimeUrl");
		AnimediaMALTitleReferences onePunchMan7_7 = AnimediaMALTitleReferences.builder().url(onePunchManUrl).dataList("7")
				.minConcretizedEpisodeOnAnimedia("7").titleOnMAL(onePunchManTitle).firstEpisode("7").maxConcretizedEpisodeOnAnimedia("7").currentMax("7")
				.posterUrl(posterUrl).minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").build();
		String finalUrl = animediaOnlineTv + onePunchMan7_7.getUrl() + "/" + onePunchMan7_7.getDataList() + "/" + onePunchMan7_7.getCurrentMax();
		for (int i = 0; i < animeNumEpisodes + 1; i++) {
			Integer numWatchedEpisodes = i;
			String episodeNumberForWatch = String.valueOf(numWatchedEpisodes + 1);
			onePunchMan.setNumWatchedEpisodes(numWatchedEpisodes);
			Set<UserMALTitleInfo> updatedWatchingTitle = new LinkedHashSet<>();
			updatedWatchingTitle.add(onePunchMan);
			Set<AnimediaMALTitleReferences> matchedReferences = new LinkedHashSet<>();
			matchedReferences.add(onePunchMan7_7);
			Set<AnimediaMALTitleReferences> allReferences = new LinkedHashSet<>();
			allReferences.add(onePunchMan7_7);
			doReturn(allReferences).when(referencesManager).getMultiSeasonsReferences();
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
			if (i == animeNumEpisodes) {
				episodeNumberForWatch = "";
				finalUrl = "";
			}
			String finalEpisodeNumberForWatchForStream = episodeNumberForWatch;
			String finalUrlForStream = finalUrl;
			assertEquals(1, matchedReferences.size());
			assertEquals(1,
					matchedReferences.stream()
							.filter(ref -> ref.getTitleOnMAL().equals(onePunchMan7_7.getTitleOnMAL()) && ref.getDataList().equals(onePunchMan7_7.getDataList())
									&& ref.getFirstEpisode().equals(onePunchMan7_7.getFirstEpisode()) && ref.getUrl().equals(onePunchMan7_7.getUrl()) && ref
									.getMinConcretizedEpisodeOnAnimedia().equals(onePunchMan7_7.getMinConcretizedEpisodeOnAnimedia()) && ref
									.getMaxConcretizedEpisodeOnAnimedia().equals(onePunchMan7_7.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
									.equals(onePunchMan7_7.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(finalEpisodeNumberForWatchForStream) && ref
									.getFinalUrl().equals(finalUrlForStream) && ref.getMinConcretizedEpisodeOnMAL()
									.equals(onePunchMan7_7.getMinConcretizedEpisodeOnMAL()) && ref.getMaxConcretizedEpisodeOnMAL()
									.equals(onePunchMan7_7.getMaxConcretizedEpisodeOnMAL())).count());
		}
	}

	@Test
	public void updateEpisodeNumberForWatchAndFinalUrlForDistinctMultiSeasonsReferenceWithStandartFirstEpisode() {
//		4.1 https://online.animedia.tv/anime/tokiyskiy-gul/1/1 tokyo ghoul
		String tokyoGhoulTitle = "tokyo ghoul";
		String tokyoGhoulUrl = "anime/tokiyskiy-gul";
		int animeNumEpisodes = 12;
		String posterUrl = myAnimeListStaticContentUrl + "tokyoGhoulPosterUrl";
		UserMALTitleInfo tokyoGhoul = new UserMALTitleInfo(0, WATCHING.getCode(), 0, tokyoGhoulTitle, animeNumEpisodes, posterUrl, "tokyoGhoulAnimeUrl");
		AnimediaMALTitleReferences tokyoGhoulReference = AnimediaMALTitleReferences.builder().url(tokyoGhoulUrl).titleOnMAL(tokyoGhoulTitle).dataList
				("1")
				.firstEpisode("1").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("12").currentMax("12").episodesRange(getEpisodesRange("1","12")).posterUrl(posterUrl).build();
		checkUpdatedMatchedReferences(tokyoGhoul, tokyoGhoulReference);
	}

	@Test
	public void updateEpisodeNumberForWatchAndFinalUrlForDistinctMultiSeasonsReferenceWithNotStandartFirstEpisode() {
//			5.2 https://online.animedia.tv/anime/skazka-o-hvoste-fei-TV1/2/176 fairy tail (2014) 176-277
		String fairyTailTitle = "fairy tail (2014)";
		String fairyTailUrl = "anime/skazka-o-hvoste-fei-TV1";
		int animeNumEpisodes = 102;
		String posterUrl = myAnimeListStaticContentUrl + "fairyTailPosterUrl";
		UserMALTitleInfo fairyTail = new UserMALTitleInfo(0, WATCHING.getCode(), 0, fairyTailTitle, animeNumEpisodes, posterUrl, "fairyTailAnimeUrl");
		AnimediaMALTitleReferences fairyTailReference = AnimediaMALTitleReferences.builder().url(fairyTailUrl).titleOnMAL(fairyTailTitle).dataList("2")
				.firstEpisode("176").minConcretizedEpisodeOnAnimedia("176").maxConcretizedEpisodeOnAnimedia("277").currentMax("277").posterUrl(posterUrl).episodesRange(getEpisodesRange("176","277"))
				.build();
		checkUpdatedMatchedReferences(fairyTail, fairyTailReference);
	}

	private void checkUpdatedMatchedReferences(UserMALTitleInfo updatedTitle, AnimediaMALTitleReferences reference) {
		Integer animeNumEpisodes = updatedTitle.getAnimeNumEpisodes();
		for (int i = 0; i < animeNumEpisodes + 1; i++) {
			Integer numWatchedEpisodes = i;
			String episodeNumberForWatch = String.valueOf(Integer.parseInt(reference.getFirstEpisode()) + numWatchedEpisodes);
			updatedTitle.setNumWatchedEpisodes(numWatchedEpisodes);
			Set<UserMALTitleInfo> updatedWatchingTitle = new LinkedHashSet<>();
			updatedWatchingTitle.add(updatedTitle);
			Set<AnimediaMALTitleReferences> matchedReferences = new LinkedHashSet<>();
			matchedReferences.add(reference);
			Set<AnimediaMALTitleReferences> allReferences = new LinkedHashSet<>();
			allReferences.add(reference);
			doReturn(allReferences).when(referencesManager).getMultiSeasonsReferences();
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(updatedWatchingTitle, matchedReferences, getAnimediaSearchList(), USERNAME);
			String finalUrl = animediaOnlineTv + reference.getUrl() + "/" + reference.getDataList() + "/" + episodeNumberForWatch;
			if (i == updatedTitle.getAnimeNumEpisodes()) {
				episodeNumberForWatch = "";
				finalUrl = "";
			}
			String finalEpisodeNumberForWatchForStream = episodeNumberForWatch;
			String finalUrlForStream = finalUrl;
			assertEquals(1, matchedReferences.size());
			assertEquals(1,
					matchedReferences.stream()
							.filter(ref -> ref.getTitleOnMAL().equals(reference.getTitleOnMAL()) && ref.getDataList().equals(reference.getDataList()) && ref
									.getFirstEpisode().equals(reference.getFirstEpisode()) && ref.getUrl().equals(reference.getUrl()) && ref
									.getMinConcretizedEpisodeOnAnimedia().equals(reference.getMinConcretizedEpisodeOnAnimedia()) && ref
									.getMaxConcretizedEpisodeOnAnimedia().equals(reference.getMaxConcretizedEpisodeOnAnimedia()) && ref.getCurrentMax()
									.equals(reference.getCurrentMax()) && ref.getEpisodeNumberForWatch().equals(finalEpisodeNumberForWatchForStream) && ref
									.getFinalUrl().equals(finalUrlForStream)).count());
		}
	}

	private void checkMatchedResult(Integer blackCloverNumWatchingEpisodes, Integer onePieceNumWatchingEpisodes, Integer anotherNumWatchingEpisodes,
			Integer onePunchManRoadToHeroNumWatchedEpisodes, Integer onePunchManSpecialsNumWatchedEpisodes, Integer sao1NumWatchedEpisodes,
			Integer tamayuraNumWatchedEpisodes, boolean isNewEpisodeAvailable) {
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(blackCloverNumWatchingEpisodes,
				onePieceNumWatchingEpisodes,
				anotherNumWatchingEpisodes,
				onePunchManRoadToHeroNumWatchedEpisodes,
				onePunchManSpecialsNumWatchedEpisodes,
				sao1NumWatchedEpisodes,
				tamayuraNumWatchedEpisodes);
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker
				.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		AnimediaMALTitleReferences blackClover = AnimediaMALTitleReferences.builder().url(BLACK_CLOVER_URL).dataList("1")
				.minConcretizedEpisodeOnAnimedia("1").titleOnMAL(BLACK_CLOVER_TITLE).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("xxx").currentMax
						("69")
				.posterUrl(myAnimeListStaticContentUrl + BLACK_CLOVER_POSTER_URL).build();
		AnimediaMALTitleReferences another = AnimediaMALTitleReferences.builder().url(ANOTHER_URL).dataList("1").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(ANOTHER_TITLE).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("12").currentMax("12")
				.posterUrl(myAnimeListStaticContentUrl + ANOTHER_POSTER_URL).build();
		AnimediaMALTitleReferences onePiece = multiSeasonsReferencesList.stream()
				.filter(ref -> ref.getTitleOnMAL().equals(ONE_PIECE_NAME) && ref.getDataList().equals("5")).findFirst().get();
		if (isNewEpisodeAvailable) {
			onePiece = multiSeasonsReferencesList.stream().filter(ref -> ref.getTitleOnMAL().equals(ONE_PIECE_NAME) && ref.getDataList().equals("1"))
					.findFirst().get();
		}
		AnimediaMALTitleReferences onePunchManRoadToHero = multiSeasonsReferencesList.stream()
				.filter(ref -> ref.getTitleOnMAL().equals(ONE_PUNCH_MAN_ROAD_TO_HERO)).findFirst().get();
		AnimediaMALTitleReferences onePunchManSpecials = multiSeasonsReferencesList.stream()
				.filter(ref -> ref.getTitleOnMAL().equals(ONE_PUNCH_MAN_SPECIALS)).findFirst().get();
		AnimediaMALTitleReferences sao1 = multiSeasonsReferencesList.stream().filter(ref -> ref.getTitleOnMAL().equals(SAO_1_TITLE)).findFirst().get();
		AnimediaMALTitleReferences tamayura = multiSeasonsReferencesList.stream()
				.filter(ref -> ref.getTitleOnMAL().equals(TAMAYURA) && Integer.parseInt(ref.getMinConcretizedEpisodeOnMAL()) <= (tamayuraNumWatchedEpisodes
						+ 1) && Integer.parseInt(ref.getMaxConcretizedEpisodeOnMAL()) >= (tamayuraNumWatchedEpisodes + 1)).findFirst().orElse(null);
		if (tamayura == null) {
			tamayura = multiSeasonsReferencesList.stream().filter(ref -> ref.getTitleOnMAL().equals(TAMAYURA))
					.sorted((x, y) -> -x.getMinConcretizedEpisodeOnMAL().compareTo(y.getMinConcretizedEpisodeOnMAL())).findFirst().get();
		}
		assertNotNull(matchedAnime);
		assertEquals(7, matchedAnime.size());
		String blackCloverEpisodeNumberForWatch = "";
		String blackCloverFinalUrl = "";
		String anotherEpisodeNumberForWatch = "";
		String anotherFinalUrl = "";
		String onePieceEpisodeNumberForWatch = "";
		String onePieceFinalUrl = "";
		String onePunchManRoadToHeroEpisodeNumberForWatch = "";
		String onePunchManRoadToHeroFinalUrl = "";
		String onePunchManSpecialsEpisodeNumberForWatch = "";
		String onePunchManSpecialsFinalUrl = "";
		String sao1EpisodeNumberForWatch = "";
		String sao1FinalUrl = "";
		String tamayuraEpisodeNumberForWatch = "";
		String tamayuraFinalUrl = "";
		if (isNewEpisodeAvailable) {
			blackCloverEpisodeNumberForWatch = String.valueOf(blackCloverNumWatchingEpisodes + 1);
			blackCloverFinalUrl =
					animediaOnlineTv + blackClover.getUrl() + "/" + blackClover.getDataList() + "/" + String.valueOf(blackCloverNumWatchingEpisodes + 1);
			anotherEpisodeNumberForWatch = String.valueOf(anotherNumWatchingEpisodes + 1);
			anotherFinalUrl = animediaOnlineTv + another.getUrl() + "/" + another.getDataList() + "/" + String.valueOf(anotherNumWatchingEpisodes + 1);
			onePieceEpisodeNumberForWatch = String.valueOf(onePieceNumWatchingEpisodes + 1);
			onePieceFinalUrl = animediaOnlineTv + onePiece.getUrl() + "/" + onePiece.getDataList() + "/" + String.valueOf(onePieceNumWatchingEpisodes + 1);
			onePunchManRoadToHeroEpisodeNumberForWatch = String.valueOf(onePunchManRoadToHeroNumWatchedEpisodes + 1);
			onePunchManRoadToHeroFinalUrl =
					animediaOnlineTv + onePunchManRoadToHero.getUrl() + "/" + onePunchManRoadToHero.getDataList() + "/" + onePunchManRoadToHero
							.getCurrentMax();
			onePunchManSpecialsEpisodeNumberForWatch = String.valueOf(onePunchManSpecialsNumWatchedEpisodes + 1);
			onePunchManSpecialsFinalUrl = animediaOnlineTv + onePunchManSpecials.getUrl() + "/" + onePunchManSpecials.getDataList() + "/" + String
					.valueOf(onePunchManSpecialsNumWatchedEpisodes + 1);
			sao1EpisodeNumberForWatch = String.valueOf(sao1NumWatchedEpisodes + 1);
			sao1FinalUrl = animediaOnlineTv + sao1.getUrl() + "/" + sao1.getDataList() + "/" + String.valueOf(sao1NumWatchedEpisodes + 1);
			tamayuraEpisodeNumberForWatch = String.valueOf(tamayuraNumWatchedEpisodes + 1);
			tamayuraFinalUrl = animediaOnlineTv + tamayura.getUrl() + "/" + tamayura.getDataList() + "/" + tamayura.getFirstEpisode();
		}
		String finalBlackCloverEpisodeNumberForWatch = blackCloverEpisodeNumberForWatch;
		String finalBlackCloverFinalUrl = blackCloverFinalUrl;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(blackClover.getTitleOnMAL()) && set.getUrl().equals(blackClover.getUrl()) && set.getDataList()
								.equals(blackClover.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia()
								.equals(blackClover.getMinConcretizedEpisodeOnAnimedia()) && set.getMaxConcretizedEpisodeOnAnimedia()
								.equals(blackClover.getMaxConcretizedEpisodeOnAnimedia()) && set.getCurrentMax().equals(blackClover.getCurrentMax()) && set
								.getEpisodeNumberForWatch().equals(finalBlackCloverEpisodeNumberForWatch) && set.getFinalUrl().equals(finalBlackCloverFinalUrl) &&
								set
								.getPosterUrl().equals(blackClover.getPosterUrl())).count());
		String finalAnotherEpisodeNumberForWatch = anotherEpisodeNumberForWatch;
		String finalAnotherFinalUrl = anotherFinalUrl;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(another.getTitleOnMAL()) && set.getUrl().equals(another.getUrl()) && set.getDataList()
								.equals(another.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia().equals(another.getMinConcretizedEpisodeOnAnimedia()) &&
								set
								.getMaxConcretizedEpisodeOnAnimedia().equals(another.getMaxConcretizedEpisodeOnAnimedia()) && set.getCurrentMax()
								.equals(another.getCurrentMax()) && set.getEpisodeNumberForWatch().equals(finalAnotherEpisodeNumberForWatch) && set.getFinalUrl()
								.equals(finalAnotherFinalUrl) && set.getPosterUrl().equals(another.getPosterUrl())).count());
		String finalOnePieceEpisodeNumberForWatch = onePieceEpisodeNumberForWatch;
		String finalOnePieceFinalUrl = onePieceFinalUrl;
		AnimediaMALTitleReferences finalOnePiece = onePiece;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(finalOnePiece.getTitleOnMAL()) && set.getUrl().equals(finalOnePiece.getUrl()) && set
								.getDataList().equals(finalOnePiece.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia()
								.equals(finalOnePiece.getMinConcretizedEpisodeOnAnimedia()) && set.getMaxConcretizedEpisodeOnAnimedia()
								.equals(finalOnePiece.getMaxConcretizedEpisodeOnAnimedia()) && set.getCurrentMax().equals(finalOnePiece.getCurrentMax()) && set
								.getEpisodeNumberForWatch().equals(finalOnePieceEpisodeNumberForWatch) && set.getFinalUrl().equals(finalOnePieceFinalUrl) && set
								.getPosterUrl().equals(finalOnePiece.getPosterUrl())).count());
		String finalOnePunchManRoadToHeroEpisodeNumberForWatch = onePunchManRoadToHeroEpisodeNumberForWatch;
		String finalOnePunchManRoadToHeroFinalUrl = onePunchManRoadToHeroFinalUrl;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(onePunchManRoadToHero.getTitleOnMAL()) && set.getUrl().equals(onePunchManRoadToHero.getUrl())
								&& set.getDataList().equals(onePunchManRoadToHero.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia()
								.equals(onePunchManRoadToHero.getMinConcretizedEpisodeOnAnimedia()) && set.getMaxConcretizedEpisodeOnAnimedia()
								.equals(onePunchManRoadToHero.getMaxConcretizedEpisodeOnAnimedia()) && set.getMinConcretizedEpisodeOnMAL()
								.equals(onePunchManRoadToHero.getMinConcretizedEpisodeOnMAL()) && set.getMaxConcretizedEpisodeOnMAL()
								.equals(onePunchManRoadToHero.getMaxConcretizedEpisodeOnMAL()) && set.getCurrentMax().equals(onePunchManRoadToHero.getCurrentMax())
								&& set.getEpisodeNumberForWatch().equals(finalOnePunchManRoadToHeroEpisodeNumberForWatch) && set.getFinalUrl()
								.equals(finalOnePunchManRoadToHeroFinalUrl) && set.getPosterUrl().equals(onePunchManRoadToHero.getPosterUrl())).count());
		String finalOnePunchManSpecialsEpisodeNumberForWatch = onePunchManSpecialsEpisodeNumberForWatch;
		String finalOnePunchManSpecialsFinalUrl = onePunchManSpecialsFinalUrl;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(onePunchManSpecials.getTitleOnMAL()) && set.getUrl().equals(onePunchManSpecials.getUrl()) &&
								set
								.getDataList().equals(onePunchManSpecials.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia()
								.equals(onePunchManSpecials.getMinConcretizedEpisodeOnAnimedia()) && set.getMaxConcretizedEpisodeOnAnimedia()
								.equals(onePunchManSpecials.getMaxConcretizedEpisodeOnAnimedia()) && set.getMinConcretizedEpisodeOnMAL()
								.equals(onePunchManSpecials.getMinConcretizedEpisodeOnMAL()) && set.getMaxConcretizedEpisodeOnMAL()
								.equals(onePunchManSpecials.getMaxConcretizedEpisodeOnMAL()) && set.getCurrentMax().equals(onePunchManSpecials.getCurrentMax()) &&
								set
								.getEpisodeNumberForWatch().equals(finalOnePunchManSpecialsEpisodeNumberForWatch) && set.getFinalUrl()
								.equals(finalOnePunchManSpecialsFinalUrl) && set.getPosterUrl().equals(onePunchManSpecials.getPosterUrl())).count());
		String finalSao1EpisodeNumberForWatch = sao1EpisodeNumberForWatch;
		String finalSao1FinalUrl = sao1FinalUrl;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(sao1.getTitleOnMAL()) && set.getUrl().equals(sao1.getUrl()) && set.getDataList()
								.equals(sao1.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia().equals(sao1.getMinConcretizedEpisodeOnAnimedia()) && set
								.getMaxConcretizedEpisodeOnAnimedia().equals(sao1.getMaxConcretizedEpisodeOnAnimedia()) && set.getCurrentMax()
								.equals(sao1.getCurrentMax()) && set.getEpisodeNumberForWatch().equals(finalSao1EpisodeNumberForWatch) && set.getFinalUrl()
								.equals(finalSao1FinalUrl) && set.getPosterUrl().equals(sao1.getPosterUrl())).count());
		String finalTamayuraEpisodeNumberForWatch = tamayuraEpisodeNumberForWatch;
		String finalTamayuraFinalUrl = tamayuraFinalUrl;
		AnimediaMALTitleReferences finalTamayura = tamayura;
		assertEquals(1,
				matchedAnime.stream()
						.filter(set -> set.getTitleOnMAL().equals(finalTamayura.getTitleOnMAL()) && set.getUrl().equals(finalTamayura.getUrl()) && set
								.getDataList().equals(finalTamayura.getDataList()) && set.getMinConcretizedEpisodeOnAnimedia()
								.equals(finalTamayura.getMinConcretizedEpisodeOnAnimedia()) && set.getMaxConcretizedEpisodeOnAnimedia()
								.equals(finalTamayura.getMaxConcretizedEpisodeOnAnimedia()) && set.getMinConcretizedEpisodeOnMAL()
								.equals(finalTamayura.getMinConcretizedEpisodeOnMAL()) && set.getMaxConcretizedEpisodeOnMAL()
								.equals(finalTamayura.getMaxConcretizedEpisodeOnMAL()) && set.getCurrentMax().equals(finalTamayura.getCurrentMax()) && set
								.getEpisodeNumberForWatch().equals(finalTamayuraEpisodeNumberForWatch) && set.getFinalUrl().equals(finalTamayuraFinalUrl) && set
								.getPosterUrl().equals(finalTamayura.getPosterUrl())).count());
		assertEquals(1, notFoundOnAnimediaRepoMock.size());
		assertEquals(NOT_FOUND_ON_ANIMEDIA_TITLE, notFoundOnAnimediaRepoMock.get(0).getTitle());
	}

	private Set<UserMALTitleInfo> getWatchingTitles(Integer blackCloverNumWatchingEpisodes, Integer onePieceNumWatchingEpisodes,
			Integer anotherNumWatchingEpisodes, Integer onePunchManRoadToHeroNumWatchedEpisodes, Integer onePunchManSpecialsNumWatchedEpisodes,
			Integer sao1NumWatchedEpisodes, Integer tamayuraNumWatchedEpisodes) {
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo notFound = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				0,
				NOT_FOUND_ON_ANIMEDIA_TITLE,
				0,
				myAnimeListStaticContentUrl + NOT_FOUND_ON_ANIMEDIA_POSTER_URL,
				"notFoundOnAnimediaAnimeUrl");
		UserMALTitleInfo single = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				blackCloverNumWatchingEpisodes,
				BLACK_CLOVER_TITLE,
				0,
				myAnimeListStaticContentUrl + BLACK_CLOVER_POSTER_URL,
				"blackCloverAnimeUrl");
		UserMALTitleInfo singleForConcretizeCase = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				anotherNumWatchingEpisodes,
				ANOTHER_TITLE,
				0,
				myAnimeListStaticContentUrl + ANOTHER_POSTER_URL,
				"anotherAnimeUrl");
		UserMALTitleInfo multi = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				onePieceNumWatchingEpisodes,
				ONE_PIECE_NAME,
				0,
				myAnimeListStaticContentUrl + ONE_PIECE_POSTER_URL,
				"onePieceAnimeUrl");
		UserMALTitleInfo onePunchManRoadToHero = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				onePunchManRoadToHeroNumWatchedEpisodes,
				ONE_PUNCH_MAN_ROAD_TO_HERO,
				1,
				myAnimeListStaticContentUrl + ONEPUNCHMAN_POSTER_URL,
				"onePunchManAnimeUrl");
		UserMALTitleInfo onePunchManSpecials = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				onePunchManSpecialsNumWatchedEpisodes,
				ONE_PUNCH_MAN_SPECIALS,
				6,
				myAnimeListStaticContentUrl + ONEPUNCHMAN_POSTER_URL,
				"onePunchManAnimeUrl");
		UserMALTitleInfo sao1 = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				sao1NumWatchedEpisodes,
				SAO_1_TITLE,
				12,
				myAnimeListStaticContentUrl + SAO_1_POSTER_URL,
				"sao1AnimeUrl");
		UserMALTitleInfo tamayura = new UserMALTitleInfo(0,
				WATCHING.getCode(),
				tamayuraNumWatchedEpisodes,
				TAMAYURA,
				4,
				myAnimeListStaticContentUrl + TAMAYURA_POSTER_URL,
				"tamayuraAnimeUrl");
		watchingTitles.add(notFound);
		watchingTitles.add(single);
		watchingTitles.add(multi);
		watchingTitles.add(singleForConcretizeCase);
		watchingTitles.add(onePunchManRoadToHero);
		watchingTitles.add(onePunchManSpecials);
		watchingTitles.add(sao1);
		watchingTitles.add(tamayura);
		return watchingTitles;
	}

	private Set<AnimediaMALTitleReferences> getReferences() {
		Set<AnimediaMALTitleReferences> references = new LinkedHashSet<>();
		String onePiecePosterUrl = myAnimeListStaticContentUrl + ONE_PIECE_POSTER_URL;
		AnimediaMALTitleReferences onePiece1 = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("1").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(ONE_PIECE_NAME).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("175").currentMax("175").posterUrl(onePiecePosterUrl)
				.episodesRange(getEpisodesRange("1","175")).build();
		AnimediaMALTitleReferences onePiece2 = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("2")
				.minConcretizedEpisodeOnAnimedia("176").titleOnMAL(ONE_PIECE_NAME).firstEpisode("176").maxConcretizedEpisodeOnAnimedia("351")
				.currentMax("351").posterUrl(onePiecePosterUrl).episodesRange(getEpisodesRange("176","351")).build();
		AnimediaMALTitleReferences onePiece3 = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("3")
				.minConcretizedEpisodeOnAnimedia("352").titleOnMAL(ONE_PIECE_NAME).firstEpisode("352").maxConcretizedEpisodeOnAnimedia("527")
				.currentMax("527").posterUrl(onePiecePosterUrl).episodesRange(getEpisodesRange("352","527")).build();
		AnimediaMALTitleReferences onePiece4 = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("4")
				.minConcretizedEpisodeOnAnimedia("528").titleOnMAL(ONE_PIECE_NAME).firstEpisode("528").maxConcretizedEpisodeOnAnimedia("700")
				.currentMax("700").posterUrl(onePiecePosterUrl).episodesRange(getEpisodesRange("528","700")).build();
		AnimediaMALTitleReferences onePiece5 = AnimediaMALTitleReferences.builder().url(ONE_PIECE_URL).dataList("5")
				.minConcretizedEpisodeOnAnimedia("701").titleOnMAL(ONE_PIECE_NAME).firstEpisode("701").maxConcretizedEpisodeOnAnimedia("xxx")
				.currentMax("870").posterUrl(onePiecePosterUrl).episodesRange(getEpisodesRange("701","870")).build();
		AnimediaMALTitleReferences onePunchMan7_7 = AnimediaMALTitleReferences.builder().url(ONE_PUNCH_MAN_URL).dataList("7")
				.titleOnMAL(ONE_PUNCH_MAN_ROAD_TO_HERO).firstEpisode("7").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.currentMax("7").posterUrl(myAnimeListStaticContentUrl + ONEPUNCHMAN_POSTER_URL).minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("1").build();
		AnimediaMALTitleReferences onePunchManSpecials = AnimediaMALTitleReferences.builder().url(ONE_PUNCH_MAN_URL).dataList("7")
				.minConcretizedEpisodeOnAnimedia("1").titleOnMAL(ONE_PUNCH_MAN_SPECIALS).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("6")
				.currentMax("6").posterUrl(myAnimeListStaticContentUrl + ONEPUNCHMAN_POSTER_URL).minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("6").build();
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder().url(SAO_1_URL).dataList("1").minConcretizedEpisodeOnAnimedia("1")
				.titleOnMAL(SAO_1_TITLE).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("12").currentMax("12")
				.posterUrl(myAnimeListStaticContentUrl + SAO_1_POSTER_URL).episodesRange(getEpisodesRange("1","12")).build();
		AnimediaMALTitleReferences tamayura1_2 = AnimediaMALTitleReferences.builder().url(TAMAYURA_URL).dataList("2").minConcretizedEpisodeOnAnimedia
				("1")
				.titleOnMAL(TAMAYURA).firstEpisode("1").maxConcretizedEpisodeOnAnimedia("1").currentMax("1")
				.posterUrl(myAnimeListStaticContentUrl + TAMAYURA_POSTER_URL).minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("2").build();
		AnimediaMALTitleReferences tamayura3_4 = AnimediaMALTitleReferences.builder().url(TAMAYURA_URL).dataList("2").minConcretizedEpisodeOnAnimedia
				("2")
				.titleOnMAL(TAMAYURA).firstEpisode("2").maxConcretizedEpisodeOnAnimedia("2").currentMax("2")
				.posterUrl(myAnimeListStaticContentUrl + TAMAYURA_POSTER_URL).minConcretizedEpisodeOnMAL("3").maxConcretizedEpisodeOnMAL("4").build();
		references.add(onePiece1);
		references.add(onePiece2);
		references.add(onePiece3);
		references.add(onePiece4);
		references.add(onePiece5);
		references.add(onePunchMan7_7);
		references.add(onePunchManSpecials);
		references.add(sao1);
		references.add(tamayura1_2);
		references.add(tamayura3_4);
		return references;
	}

	private Set<AnimediaTitleSearchInfo> getAnimediaSearchList() {
		return RoutinesIO.unmarshalFromResource(animediaSearchListSeveralTitlesMatchedForKeywords, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
	}
}