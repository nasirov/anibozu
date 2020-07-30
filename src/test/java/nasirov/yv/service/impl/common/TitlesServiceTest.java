package nasirov.yv.service.impl.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.impl.common.TitlesService;
import nasirov.yv.utils.CommonTitleTestBuilder;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class TitlesServiceTest {

	@Mock
	private GitHubResourcesServiceI gitHubResourcesService;

	@Mock
	private GitHubResourceProps gitHubResourceProps;

	@InjectMocks
	private TitlesService titlesService;

	@Test
	public void shouldReturnCommonTitles() {
		//given
		mockFandubResourcesNames();
		mockGitHubResourcesService(CommonTitleTestBuilder.buildCommonTitles(FanDubSource.ANIMEDIA));
		//when
		Map<Integer, List<CommonTitle>> result = titlesService.getTitles(FanDubSource.ANIMEDIA);
		//then
		assertEquals(2, result.size());
		checkResult(CommonTitleTestBuilder.getAnimediaRegular(), result);
		checkResult(CommonTitleTestBuilder.getAnimediaConcretized(), result);
	}

	@Test
	public void shouldReturnEmptyMap() {
		//given
		mockFandubResourcesNames();
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<CommonTitle>> result = titlesService.getTitles(FanDubSource.ANIMEDIA);
		//then
		assertTrue(result.isEmpty());
	}

	private void mockFandubResourcesNames() {
		doReturn(Maps.newHashMap(FanDubSource.ANIMEDIA, "resourceName.json")).when(gitHubResourceProps)
				.getResourcesNames();
	}

	private void mockGitHubResourcesService(List<CommonTitle> commonTitles) {
		doReturn(commonTitles).when(gitHubResourcesService)
				.getResource("resourceName.json");
	}

	private void checkResult(CommonTitle expectedCommonTitle, Map<Integer, List<CommonTitle>> result) {
		List<CommonTitle> actualCommonTitleList = result.get(expectedCommonTitle.getMalId());
		assertEquals(1, actualCommonTitleList.size());
		CommonTitle actual = actualCommonTitleList.get(0);
		assertEquals(expectedCommonTitle, actual);
	}
}