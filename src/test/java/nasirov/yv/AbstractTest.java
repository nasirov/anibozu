package nasirov.yv;

import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.data.properties.UrlsNames;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by nasirov.yv
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@EnableConfigurationProperties({ResourcesNames.class, UrlsNames.class})
public abstract class AbstractTest {

	protected static final String SAO_ID = "9432";

	protected static final int TEST_ACC_WATCHING_TITLES = 1004;

	protected static final String FAIRY_TAIL_ROOT_URL = "anime/skazka-o-hvoste-fei-TV1";

	protected static final String SAO_ROOT_URL = "anime/mastera-mecha-onlayn";

	protected static final String TITANS_ROOT_URL = "anime/vtorjenie-gigantov";

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

	@Value("classpath:animedia/search/pageWithCurrentlyAdded10Episodes.txt")
	protected Resource pageWithCurrentlyAddedEpisodes;

	@Value("classpath:animedia/singleSeason/blackCloverHtml.txt")
	protected Resource blackCloverHtml;

	@Value("classpath:mal/testAccForDevProfile.txt")
	protected Resource testAccForDevProfile;

	@Autowired
	protected ResourcesNames resourcesNames;

	@Autowired
	protected UrlsNames urlsNames;

	protected String tempFolderName;

	protected String myAnimeListStaticContentUrl;

	protected String animediaOnlineTv;

	protected String animediaEpisodesList;

	protected String animediaEpisodesListPostfix;

	@Before
	public void setUp() {
		tempFolderName = resourcesNames.getTempFolder();
		myAnimeListStaticContentUrl = urlsNames.getMalUrls().getCdnMyAnimeListNet();
		animediaOnlineTv = urlsNames.getAnimediaUrls().getOnlineAnimediaTv();
		animediaEpisodesList = urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesList();
		animediaEpisodesListPostfix = urlsNames.getAnimediaUrls().getOnlineAnimediaAnimeEpisodesPostfix();
	}

}
