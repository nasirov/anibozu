package nasirov.yv.service;

import nasirov.yv.AbstractTest;
import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.util.RoutinesIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static nasirov.yv.enums.MALAnimeStatus.WATCHING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {AppConfiguration.class,
		SeasonAndEpisodeChecker.class,
		AnimediaRequestParametersBuilder.class,
		AnimediaHTMLParser.class,
		RoutinesIO.class,
		WrappedObjectMapper.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SeasonAndEpisodeCheckerTest extends AbstractTest {
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
	
	@MockBean
	private HttpCaller httpCaller;
	
	@MockBean
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;
	
	@MockBean
	private ReferencesManager referencesManager;
	
	@Autowired
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	private List<UserMALTitleInfo> notFoundOnAnimediaRepoStub;
	
	@Before
	public void setUp() {
		notFoundOnAnimediaRepoStub = new ArrayList<>();
		doAnswer(answer -> {
			notFoundOnAnimediaRepoStub.add(answer.getArgument(0));
			return (answer.getArgument(0));
		}).when(notFoundAnimeOnAnimediaRepository).saveAndFlush(any(UserMALTitleInfo.class));
		doAnswer(answer -> notFoundOnAnimediaRepoStub.stream().filter(list -> String.valueOf(list.getTitle()).equals(answer.getArgument(0))).count() > 0).when(notFoundAnimeOnAnimediaRepository).exitsByTitle(anyString());
		doReturn(new HttpResponse(routinesIO.readFromResource(blackCloverHtml), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv + BLACK_CLOVER_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(blackCloverDataList1), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + BLACK_CLOVER_ID + DATA_LIST_1), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(anotherHtml), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv + ANOTHER_URL), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(anotherDataList1), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + ANOTHER_ID + DATA_LIST_1), eq(HttpMethod.GET), anyMap());
	}
	
	@Test
	public void getMatchedAnimeNewEpisodesAvailable() throws Exception {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(0, 0, 0);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertNotNull(matchedAnime);
		assertEquals(3, matchedAnime.size());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(BLACK_CLOVER_TITLE)
						&& set.getUrl().equals(BLACK_CLOVER_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("xxx")
						&& set.getCurrentMax().equals("69")
						&& set.getEpisodeNumberForWatch().equals("1")
						&& set.getFinalUrl().equals(animediaOnlineTv + BLACK_CLOVER_URL + DATA_LIST_1_EPISODE_1)
				).count());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ANOTHER_TITLE)
						&& set.getUrl().equals(ANOTHER_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("12")
						&& set.getCurrentMax().equals("12")
						&& set.getEpisodeNumberForWatch().equals("1")
						&& set.getFinalUrl().equals(animediaOnlineTv + ANOTHER_URL + DATA_LIST_1_EPISODE_1)
				).count());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ONE_PIECE_NAME)
						&& set.getUrl().equals(ONE_PIECE_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("175")
						&& set.getCurrentMax().equals("175")
						&& set.getEpisodeNumberForWatch().equals("1")
						&& set.getFinalUrl().equals(animediaOnlineTv + ONE_PIECE_URL + DATA_LIST_1_EPISODE_1)
				).count());
		assertEquals(1, notFoundOnAnimediaRepoStub.size());
		assertEquals("notFoundOnAnimedia", notFoundOnAnimediaRepoStub.get(0).getTitle());
	}
	
	@Test
	public void getMatchedAnimeNewEpisodesNotAvailable() throws Exception {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(69, 870, 12);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertNotNull(matchedAnime);
		assertEquals(3, matchedAnime.size());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(BLACK_CLOVER_TITLE)
						&& set.getUrl().equals(BLACK_CLOVER_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("xxx")
						&& set.getCurrentMax().equals("69")
						&& set.getEpisodeNumberForWatch().equals("")
						&& set.getFinalUrl().equals("")
				).count());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ANOTHER_TITLE)
						&& set.getUrl().equals(ANOTHER_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("12")
						&& set.getCurrentMax().equals("12")
						&& set.getEpisodeNumberForWatch().equals("")
						&& set.getFinalUrl().equals("")
				).count());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ONE_PIECE_NAME)
						&& set.getUrl().equals(ONE_PIECE_URL)
						&& set.getDataList().equals("5")
						&& set.getMin().equals("701")
						&& set.getMax().equals("xxx")
						&& set.getCurrentMax().equals("870")
						&& set.getEpisodeNumberForWatch().equals("")
						&& set.getFinalUrl().equals("")
				).count());
		assertEquals(1, notFoundOnAnimediaRepoStub.size());
		assertEquals("notFoundOnAnimedia", notFoundOnAnimediaRepoStub.get(0).getTitle());
	}
	
	@Test
	public void getMatchedAnimeMoreThanOneMatchesInAnimediaSearchList() {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(0, 0, 0);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		AnimediaTitleSearchInfo singleWithEqualsKeywords = new AnimediaTitleSearchInfo("another smth to test", "another", "anime/another-smth-to-test", "anotherSmthToTestUrl");
		animediaSearchList.add(singleWithEqualsKeywords);
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertNotNull(matchedAnime);
		assertEquals(2, matchedAnime.size());
		assertEquals(0, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ANOTHER_TITLE)
						&& set.getUrl().equals(ANOTHER_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("12")
						&& set.getCurrentMax().equals("12")
						&& set.getEpisodeNumberForWatch().equals("1")
						&& set.getFinalUrl().equals(animediaOnlineTv + ANOTHER_URL + DATA_LIST_1_EPISODE_1)
				).count());
		assertEquals(1, notFoundOnAnimediaRepoStub.size());
		assertEquals("notFoundOnAnimedia", notFoundOnAnimediaRepoStub.get(0).getTitle());
	}
	
	@Test
	public void getMatchedAnimeMoreThanOneMatchesInMultiSeasonsReferences() {
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(0, 800, 0);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, USERNAME);
		assertNotNull(matchedAnime);
		assertEquals(3, matchedAnime.size());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ONE_PIECE_NAME)
						&& set.getUrl().equals(ONE_PIECE_URL)
						&& set.getDataList().equals("5")
						&& set.getMin().equals("701")
						&& set.getMax().equals("xxx")
						&& set.getCurrentMax().equals("870")
						&& set.getEpisodeNumberForWatch().equals("801")
						&& set.getFinalUrl().equals(animediaOnlineTv + ONE_PIECE_URL + "/5/801")
				).count());
		assertEquals(1, notFoundOnAnimediaRepoStub.size());
		assertEquals("notFoundOnAnimedia", notFoundOnAnimediaRepoStub.get(0).getTitle());
	}
	
	@Test
	public void getMatchedAnimeUpdateMultiseasonsReferences() {
		String username = "testUsername";
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = new LinkedHashSet<>();
		AnimediaMALTitleReferences notUpdatedReference = new AnimediaMALTitleReferences(ONE_PIECE_URL, "1", "1", ONE_PIECE_NAME, null, null, "10", null, null, null);
		multiSeasonsReferencesList.add(notUpdatedReference);
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo addedTitleInUserCachedPeriod = new UserMALTitleInfo(0, WATCHING.getCode(), 0, ONE_PIECE_NAME, 0, "onePiecePosterUrl", "onePieceAnimeUrl");
		watchingTitles.add(addedTitleInUserCachedPeriod);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, username);
		verify(referencesManager, times(1)).updateReferences(anySet());
		assertEquals(addedTitleInUserCachedPeriod.getPosterUrl(), notUpdatedReference.getPosterUrl());
	}
	
	@Test
	public void updateMatchedReferences() throws Exception {
		Set<AnimediaMALTitleReferences> matchedReferences = getReferences();
		for (AnimediaMALTitleReferences references : matchedReferences) {
			int numWatchedEpisodes = Integer.parseInt(references.getFirstEpisode()) + 1;
			String dataList = references.getDataList();
			String firstEpisodeAndMin = references.getFirstEpisode();
			String max = references.getMax();
			String currentMax = references.getCurrentMax();
			String nextEpisodeForWatch = String.valueOf(numWatchedEpisodes + 1);
			Set<UserMALTitleInfo> watchingTitlesFresh = getWatchingTitles(5, numWatchedEpisodes, 0);
			AnimediaMALTitleReferences onePiece3 = new AnimediaMALTitleReferences(ONE_PIECE_URL, dataList, firstEpisodeAndMin, ONE_PIECE_NAME, firstEpisodeAndMin, max, currentMax, "onePiecePosterUrl", null, null);
			seasonAndEpisodeChecker.updateEpisodeNumberForWatchAndFinalUrl(watchingTitlesFresh, onePiece3, matchedReferences);
			assertEquals(1, matchedReferences.stream()
					.filter(set -> set.getTitleOnMAL().equals(ONE_PIECE_NAME)
							&& set.getUrl().equals(ONE_PIECE_URL)
							&& set.getDataList().equals(dataList)
							&& set.getMin().equals(firstEpisodeAndMin)
							&& set.getMax().equals(max)
							&& set.getCurrentMax().equals(currentMax)
							&& set.getEpisodeNumberForWatch().equals(nextEpisodeForWatch)
							&& set.getFinalUrl().equals(animediaOnlineTv + ONE_PIECE_URL + "/" + dataList + "/" + nextEpisodeForWatch)
					).count());
		}
	}
	
	private Set<UserMALTitleInfo> getWatchingTitles(Integer blackCloverNumWatchingEpisodes, Integer onePieceNumWatchingEpisodes, Integer anotherNumWatchingEpisodes) {
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo notFound = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "notFoundOnAnimedia", 0, "notFoundOnAnimediaPosterUrl", "notFoundOnAnimediaAnimeUrl");
		UserMALTitleInfo single = new UserMALTitleInfo(0, WATCHING.getCode(), blackCloverNumWatchingEpisodes, "black clover", 0, "blackCloverPosterUrl", "blackCloverAnimeUrl");
		UserMALTitleInfo singleForConcretizeCase = new UserMALTitleInfo(0, WATCHING.getCode(), anotherNumWatchingEpisodes, "another", 0, "anotherPosterUrl", "anotherAnimeUrl");
		UserMALTitleInfo multi = new UserMALTitleInfo(0, WATCHING.getCode(), onePieceNumWatchingEpisodes, ONE_PIECE_NAME, 0, "onePiecePosterUrl", "onePieceAnimeUrl");
		watchingTitles.add(notFound);
		watchingTitles.add(single);
		watchingTitles.add(multi);
		watchingTitles.add(singleForConcretizeCase);
		return watchingTitles;
	}
	
	private Set<AnimediaMALTitleReferences> getReferences() {
		Set<AnimediaMALTitleReferences> references = new LinkedHashSet<>();
		AnimediaMALTitleReferences onePiece1 = new AnimediaMALTitleReferences(ONE_PIECE_URL, "1", "1", ONE_PIECE_NAME, "1", "175", "175", "onePiecePosterUrl", null, null);
		AnimediaMALTitleReferences onePiece2 = new AnimediaMALTitleReferences(ONE_PIECE_URL, "2", "176", ONE_PIECE_NAME, "176", "351", "351", "onePiecePosterUrl", null, null);
		AnimediaMALTitleReferences onePiece3 = new AnimediaMALTitleReferences(ONE_PIECE_URL, "3", "352", ONE_PIECE_NAME, "352", "527", "527", "onePiecePosterUrl", null, null);
		AnimediaMALTitleReferences onePiece4 = new AnimediaMALTitleReferences(ONE_PIECE_URL, "4", "528", ONE_PIECE_NAME, "528", "700", "700", "onePiecePosterUrl", null, null);
		AnimediaMALTitleReferences onePiece5 = new AnimediaMALTitleReferences(ONE_PIECE_URL, "5", "701", ONE_PIECE_NAME, "701", "xxx", "870", "onePiecePosterUrl", null, null);
		references.add(onePiece1);
		references.add(onePiece2);
		references.add(onePiece3);
		references.add(onePiece4);
		references.add(onePiece5);
		return references;
	}
	
	private Set<AnimediaTitleSearchInfo> getAnimediaSearchList() {
		return routinesIO.unmarshalFromResource(animediaSearchListSeveralTitlesMatchedForKeywords, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
	}
}