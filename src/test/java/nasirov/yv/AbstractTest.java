package nasirov.yv;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:system.properties")
public abstract class AbstractTest {

	protected static final String SAO_ID = "9432";

	protected static final int TEST_ACC_WATCHING_TITLES = 651;

	@Value("${resources.tempFolder.name}")
	protected String tempFolderName;

	@Value("${resources.tempRawReferences.name}")
	protected String tempRawReferencesName;

	@Value("${resources.tempNewTitlesInAnimediaSearchList.name}")
	protected String tempNewTitlesInAnimediaSearchList;

	@Value("${resources.tempRemovedTitlesFromAnimediaSearchList.name}")
	protected String tempRemovedTitlesFromAnimediaSearchList;

	@Value("${resources.tempDuplicatedUrlsInAnimediaSearchList.name}")
	protected String tempDuplicatedUrlsInAnimediaSearchList;

	@Value("${resources.tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList.name}")
	protected String tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList;

	@Value("${cache.animediaSearchList.name}")
	protected String animediaSearchListCacheName;

	@Value("${cache.currentlyUpdatedTitles.name}")
	protected String currentlyUpdatedTitlesCacheName;

	@Value("${cache.sortedAnimediaSearchList.name}")
	protected String sortedAnimediaSearchListCacheName;

	@Value("${cache.userMatchedAnime.name}")
	protected String userMatchedAnimeCacheName;

	@Value("${urls.online.animedia.anime.list}")
	protected String animediaAnimeList;

	@Value("${cache.userMAL.name}")
	protected String userMALCacheName;

	@Value("${cache.multiSeasonsReferences.name}")
	protected String multiSeasonsReferencesCacheName;

	@Value("${urls.myAnimeList.net}")
	protected String myAnimeListNet;

	@Value("${urls.cdn.myAnimeList.net}")
	protected String myAnimeListStaticContentUrl;

	@Value("${urls.online.animedia.tv}")
	protected String animediaOnlineTv;

	@Value("${urls.online.animedia.anime.episodes.list}")
	protected String animediaEpisodesList;

	@Value("${urls.online.animedia.anime.episodes.postfix}")
	protected String animediaEpisodesListPostfix;

	@Value("${cache.matchedReferences.name}")
	protected String matchedReferencesCacheName;

	@Value("${resources.tempReferencesWithInvalidMALTitleName.name}")
	protected String tempReferencesWithInvalidMALTitleName;

	@Value("${resources.tempSearchTitlesWithInvalidMALTitleName.name}")
	protected String tempSearchTitlesWithInvalidMALTitleName;

	@Value("classpath:animedia/fairyTail/fairyTailHtml.txt")
	protected Resource fairyTailHtml;

	@Value("classpath:animedia/fairyTail/fairyTail1.txt")
	protected Resource fairyTailDataList1;

	@Value("classpath:animedia/fairyTail/fairyTail2.txt")
	protected Resource fairyTailDataList2;

	@Value("classpath:animedia/fairyTail/fairyTail3.txt")
	protected Resource fairyTailDataList3;

	@Value("classpath:animedia/fairyTail/fairyTail7.txt")
	protected Resource fairyTailDataList7;

	@Value("classpath:animedia/onePiece/onePieceHtml.txt")
	protected Resource onePieceHtml;

	@Value("classpath:animedia/onePiece/onePiece1.txt")
	protected Resource onePieceDataList1;

	@Value("classpath:animedia/onePiece/onePiece2.txt")
	protected Resource onePieceDataList2;

	@Value("classpath:animedia/onePiece/onePiece3.txt")
	protected Resource onePieceDataList3;

	@Value("classpath:animedia/onePiece/onePiece4.txt")
	protected Resource onePieceDataList4;

	@Value("classpath:animedia/onePiece/onePiece5.txt")
	protected Resource onePieceDataList5;

	@Value("classpath:animedia/sao/saoHtml.txt")
	protected Resource saoHtml;

	@Value("classpath:animedia/sao/sao1.txt")
	protected Resource saoDataList1;

	@Value("classpath:animedia/sao/sao2.txt")
	protected Resource saoDataList2;

	@Value("classpath:animedia/sao/sao3.txt")
	protected Resource saoDataList3;

	@Value("classpath:animedia/sao/sao7.txt")
	protected Resource saoDataList7;

	@Value("classpath:animedia/search/animediaSearchListForCheck.json")
	protected Resource animediaSearchListForCheck;

	@Value("classpath:animedia/search/animediaSearchListFull.json")
	protected Resource animediaSearchListFull;

	@Value("classpath:animedia/search/animediaSearchListSeveralTitlesMatchedForKeywords.json")
	protected Resource animediaSearchListSeveralTitlesMatchedForKeywords;

	@Value("classpath:animedia/search/announcements.json")
	protected Resource announcementsJson;

	@Value("classpath:animedia/search/multiSeasonsAnimeUrls.json")
	protected Resource multiSeasonsAnimeUrls;

	@Value("classpath:animedia/search/pageWithCurrentlyAddedEpisodes.txt")
	protected Resource pageWithCurrentlyAddedEpisodes;

	@Value("classpath:animedia/search/singleSeasonsAnimeUrls.json")
	protected Resource singleSeasonsAnimeUrls;

	@Value("classpath:animedia/singleSeason/blackCloverHtml.txt")
	protected Resource blackCloverHtml;

	@Value("classpath:animedia/singleSeason/blackClover1.txt")
	protected Resource blackCloverDataList1;

	@Value("classpath:animedia/singleSeason/anotherHtml.txt")
	protected Resource anotherHtml;

	@Value("classpath:animedia/titans/titans3ConcretizedAndOngoing.txt")
	protected Resource titans3ConcretizedAndOngoing;

	@Value("classpath:animedia/titans/titansHtml.txt")
	protected Resource titansHtml;

	@Value("classpath:animedia/singleSeason/another1.txt")
	protected Resource anotherDataList1;

	@Value("classpath:animedia/ingressHtml.txt")
	protected Resource announcementHtml;

	@Value("classpath:mal/testAccForDevAdditionalJson.json")
	protected Resource testAccForDevAdditionalJson;

	@Value("classpath:mal/additionalAnimeListJson.json")
	protected Resource additionalAnimeListJson;

	@Value("classpath:mal/testAccForDevProfile.txt")
	protected Resource testAccForDevProfile;

	@Value("classpath:mal/testAccForDevWatchingTitles.txt")
	protected Resource testAccForDevWatchingTitles;

	@Value("classpath:mal/searchTitleFairyTail.json")
	protected Resource searchTitleFairyTail;

	@Value("classpath:mal/searchTitleNotFound.json")
	protected Resource searchTitleNotFound;

	@Value("classpath:referencesForTest.json")
	protected Resource referencesForTestResource;

	@Value("classpath:${resources.animediaSearchList.name}")
	protected Resource resourceAnimediaSearchList;
}
