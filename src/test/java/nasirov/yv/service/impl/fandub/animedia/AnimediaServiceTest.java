package nasirov.yv.service.impl.fandub.animedia;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.fandub.animedia.AnimediaSearchListTitle;
import nasirov.yv.fandub.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.feign.fandub.animedia.AnimediaFeignClient;
import nasirov.yv.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Created by nasirov.yv
 */
public class AnimediaServiceTest extends AbstractTest {

	@MockBean
	protected AnimediaFeignClient animediaFeignClient;

	private AnimediaService animediaService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		animediaService = new AnimediaService(animediaFeignClient, animediaProps);
		animediaService.init();
	}

	@Test
	public void shouldReturnAnimediaSearchList() {
		//given
		doReturn(IOUtils.readFromFile("classpath:__files/animedia/search/aslFromSite.html")).when(animediaFeignClient)
				.getTitles(0, 2);
		doReturn(IOUtils.readFromFile("classpath:__files/animedia/regular/regularTitle.html")).when(animediaFeignClient)
				.getTitlePage(REGULAR_TITLE_URL);
		doReturn("").when(animediaFeignClient)
				.getTitlePage(ANNOUNCEMENT_TITLE_URL);
		//when
		Set<AnimediaSearchListTitle> animediaSearchList = animediaService.getAnimediaSearchList();
		//then
		assertEquals(2, animediaSearchList.size());
	}
	@Test
	public void shouldReturnDataLists() {
		//given
		doReturn(IOUtils.readFromFile("classpath:__files/animedia/regular/regularTitle.html")).when(animediaFeignClient)
				.getTitlePage(REGULAR_TITLE_URL);
		List<String> expectedSeasons = expectedSeasons();
		//when
		List<String> dataLists = animediaService.getDataLists(buildAnimediaSearchListTitle());
		//then
		assertEquals(expectedSeasons.size(), dataLists.size());
		expectedSeasons.forEach(x -> assertTrue(dataLists.contains(x)));
	}

	@Test
	public void shouldReturnDataListEpisodes() {
		//given
		String dataList = "1";
		doReturn(IOUtils.unmarshal(IOUtils.readFromFile("classpath:__files/animedia/regular/regularTitleDataList1.html"),
				AnimediaEpisode.class,
				ArrayList.class)).when(animediaFeignClient)
				.getTitleEpisodesByPlaylist(Integer.parseInt(REGULAR_TITLE_ID), Integer.parseInt(dataList));
		List<String> expectedEpisodes = expectedEpisodes();
		//when
		List<String> dataListEpisodes = animediaService.getEpisodes(REGULAR_TITLE_ID, dataList);
		//then
		assertEquals(expectedEpisodes.size(), dataListEpisodes.size());
		expectedEpisodes.forEach(x -> assertTrue(dataListEpisodes.contains(x)));
	}

	private List<String> expectedEpisodes() {
		return Lists.newArrayList("Серия 1", "Серия 2", "Серия 3", "Серия 4", "Серия 5");
	}

	private List<String> expectedSeasons() {
		return Lists.newArrayList("1", "2", "3", "7");
	}

	private AnimediaSearchListTitle buildAnimediaSearchListTitle() {
		return AnimediaSearchListTitle.builder()
				.animeId(REGULAR_TITLE_ID)
				.url(REGULAR_TITLE_URL)
				.build();
	}
}