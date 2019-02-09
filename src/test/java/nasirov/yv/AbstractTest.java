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
	
	@Value("${urls.myAnimeList.net}")
	protected String myAnimeListNet;
	
	@Value("${urls.online.animedia.tv}")
	protected String animediaOnlineTv;
	
	@Value("${urls.online.animedia.anime.episodes.list}")
	protected String animediaEpisodesList;
	
	@Value("${cache.matchedReferences.name}")
	protected String matchedReferencesCacheName;
	
	@Value("classpath:animedia/fairyTail/fairyTailHtml.txt")
	protected Resource fairyTailHtml;
	
	@Value("classpath:animedia/fairyTail/fairyTail1.txt")
	protected Resource fairyTail1;
	
	@Value("classpath:animedia/fairyTail/fairyTail2.txt")
	protected Resource fairyTail2;
	
	@Value("classpath:animedia/fairyTail/fairyTail3.txt")
	protected Resource fairyTail3;
	
	@Value("classpath:animedia/fairyTail/fairyTail7.txt")
	protected Resource fairyTail7;
	
	@Value("classpath:animedia/onePiece/onePieceHtml.txt")
	protected Resource onePieceHtml;
	
	@Value("classpath:animedia/onePiece/onePiece1.txt")
	protected Resource onePiece1;
	
	@Value("classpath:animedia/onePiece/onePiece2.txt")
	protected Resource onePiece2;
	
	@Value("classpath:animedia/onePiece/onePiece3.txt")
	protected Resource onePiece3;
	
	@Value("classpath:animedia/onePiece/onePiece4.txt")
	protected Resource onePiece4;
	
	@Value("classpath:animedia/onePiece/onePiece5.txt")
	protected Resource onePiece5;
	
	@Value("classpath:animedia/sao/saoHtml.txt")
	protected Resource saoHtml;
	
	@Value("classpath:animedia/sao/sao1.txt")
	protected Resource sao1;
	
	@Value("classpath:animedia/sao/sao2.txt")
	protected Resource sao2;
	
	@Value("classpath:animedia/sao/sao3.txt")
	protected Resource sao3;
	
	@Value("classpath:animedia/sao/sao7.txt")
	protected Resource sao7;
	
	@Value("classpath:animedia/search/animediaSearchListForCheck.json")
	protected Resource animediaSearchListForCheck;
	
	@Value("classpath:animedia/search/animediaSearchListFull.json")
	protected Resource animediaSearchListFull;
	
	@Value("classpath:animedia/search/announcements.json")
	protected Resource announcementsJson;
	
	@Value("classpath:animedia/search/multiSeasonsAnimeUrls.json")
	protected Resource multiSeasonsAnimeUrls;
	
	@Value("classpath:animedia/search/pageWithCurrentlyAddedEpisodes.txt")
	protected Resource pageWithCurrentlyAddedEpisodes;
	
	@Value("classpath:animedia/search/singleSeasonsAnimeUrls.json")
	protected Resource singleSeasonsAnimeUrls;
	
	@Value("classpath:animedia/blackCloverHtml.txt")
	protected Resource singleSeasonHtml;
	
	@Value("classpath:animedia/blackClover1.txt")
	protected Resource blackClover1;
	
	@Value("classpath:animedia/ingressHtml.txt")
	protected Resource announcementHtml;
	
	@Value("classpath:mal/testAccForDevAdditionalJson.json")
	protected Resource testAccForDevAdditionalJson;
	
	@Value("classpath:mal/testAccForDevProfile.txt")
	protected Resource testAccForDevProfile;
	
	@Value("classpath:mal/testAccForDevWatchingTitles.txt")
	protected Resource testAccForDevWatchingTitles;
	
	@Value("classpath:rawReferencesForTest.txt")
	protected Resource rawReferencesForTestResource;
}
