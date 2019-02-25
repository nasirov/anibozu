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
import static org.mockito.ArgumentMatchers.*;
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
	@MockBean
	private HttpCaller httpCaller;
	
	@MockBean
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;
	
	@MockBean
	private ReferencesManager referencesManager;
	
	private List<UserMALTitleInfo> repoMock;
	
	@Autowired
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	private static final String ONE_PIECE_URL = "anime/one-piece-van-pis-tv";
	
	private static final String ONE_PIECE_NAME = "one piece";
	
	@Before
	public void setUp() {
		repoMock = new ArrayList<>();
		doAnswer(answer -> {
			repoMock.add(answer.getArgument(0));
			return (answer.getArgument(0));
		}).when(notFoundAnimeOnAnimediaRepository).saveAndFlush(any(UserMALTitleInfo.class));
		doAnswer(answer -> repoMock.stream().filter(list -> String.valueOf(list.getTitle()).equals(answer.getArgument(0))).count() > 0).when(notFoundAnimeOnAnimediaRepository).exitsByTitle(anyString());
	}
	
	@Test
	public void getMatchedAnime() throws Exception {
		String username = "testUsername";
		String singleUrl = "anime/chyornyj-klever";
		String blackCloverId = "15341";
		String blackCloverUrl = "black clover";
		doReturn(new HttpResponse(routinesIO.readFromResource(singleSeasonHtml), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaOnlineTv + singleUrl), eq(HttpMethod.GET), anyMap());
		doReturn(new HttpResponse(routinesIO.readFromResource(blackClover1), HttpStatus.OK.value())).when(httpCaller).call(eq(animediaEpisodesList + blackCloverId + "/1"), eq(HttpMethod.GET), anyMap());
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = getReferences();
		Set<UserMALTitleInfo> watchingTitles = getWatchingTitles(0, 0);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		Set<AnimediaMALTitleReferences> matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, username);
		assertNotNull(matchedAnime);
		assertEquals(2, matchedAnime.size());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(blackCloverUrl)
						&& set.getUrl().equals(singleUrl)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("xxx")
						&& set.getCurrentMax().equals("69")
						&& set.getEpisodeNumberForWatch().equals("1")
						&& set.getFinalUrl().equals(animediaOnlineTv + singleUrl + "/1/1")
				).count());
		assertEquals(1, matchedAnime.stream()
				.filter(set -> set.getTitleOnMAL().equals(ONE_PIECE_NAME)
						&& set.getUrl().equals(ONE_PIECE_URL)
						&& set.getDataList().equals("1")
						&& set.getMin().equals("1")
						&& set.getMax().equals("175")
						&& set.getCurrentMax().equals("175")
						&& set.getEpisodeNumberForWatch().equals("1")
						&& set.getFinalUrl().equals(animediaOnlineTv + ONE_PIECE_URL + "/1/1")
				).count());
		assertEquals(1, repoMock.size());
		assertEquals("notFoundOnAnimedia", repoMock.get(0).getTitle());
		watchingTitles = getWatchingTitles(0, 800);
		matchedAnime = seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, username);
		assertNotNull(matchedAnime);
		assertEquals(2, matchedAnime.size());
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
		assertEquals(1, repoMock.size());
		assertEquals("notFoundOnAnimedia", repoMock.get(0).getTitle());
	}
	
	@Test
	public void getMatchedAnimeUpdateMultiseasonsReferences() {
		String username = "testUsername";
		Set<AnimediaMALTitleReferences> multiSeasonsReferencesList = new LinkedHashSet<>();
		AnimediaMALTitleReferences notUpdatedReference = new AnimediaMALTitleReferences(ONE_PIECE_URL, "1", "1", ONE_PIECE_NAME, null,null,"10", null, null, null);
		multiSeasonsReferencesList.add(notUpdatedReference);
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo addedTitleInUserCachedPeriod = new UserMALTitleInfo(0, WATCHING.getCode(), 0, ONE_PIECE_NAME, 0, "onePiecePosterUrl", "onePieceAnimeUrl");
		watchingTitles.add(addedTitleInUserCachedPeriod);
		Set<AnimediaTitleSearchInfo> animediaSearchList = getAnimediaSearchList();
		seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, multiSeasonsReferencesList, animediaSearchList, username);
		verify(referencesManager,times(1)).updateReferences(anySet());
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
			Set<UserMALTitleInfo> watchingTitlesFresh = getWatchingTitles(5, numWatchedEpisodes);
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
	
	private Set<UserMALTitleInfo> getWatchingTitles(Integer blackCloverNumWatchingEpisodes, Integer onePieceNumWatchingEpisodes) {
		Set<UserMALTitleInfo> watchingTitles = new LinkedHashSet<>();
		UserMALTitleInfo notFound = new UserMALTitleInfo(0, WATCHING.getCode(), 0, "notFoundOnAnimedia", 0, "notFoundOnAnimediaPosterUrl", "notFoundOnAnimediaAnimeUrl");
		UserMALTitleInfo single = new UserMALTitleInfo(0, WATCHING.getCode(), blackCloverNumWatchingEpisodes, "black clover", 0, "blackCloverPosterUrl", "blackCloverAnimeUrl");
		UserMALTitleInfo multi = new UserMALTitleInfo(0, WATCHING.getCode(), onePieceNumWatchingEpisodes, ONE_PIECE_NAME, 0, "onePiecePosterUrl", "onePieceAnimeUrl");
		watchingTitles.add(notFound);
		watchingTitles.add(single);
		watchingTitles.add(multi);
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
		Set<AnimediaTitleSearchInfo> animediaTitleSearchInfo = new LinkedHashSet<>();
		AnimediaTitleSearchInfo single = new AnimediaTitleSearchInfo("чёрный клевер", "чёрный клевер black clover черный клевер", "anime/chyornyj-klever", "blackCloverPosterUrl");
		animediaTitleSearchInfo.add(single);
		return animediaTitleSearchInfo;
	}
}