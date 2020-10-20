package nasirov.yv.service.impl.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.service.GitHubResourcesServiceI;
import nasirov.yv.utils.CommonTitleTestBuilder;
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

	@InjectMocks
	private TitlesService titlesService;

	@Test
	public void shouldReturnCommonTitles() {
		//given
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
		mockGitHubResourcesService(Collections.emptyList());
		//when
		Map<Integer, List<CommonTitle>> result = titlesService.getTitles(FanDubSource.ANIMEDIA);
		//then
		assertTrue(result.isEmpty());
	}

	private void mockGitHubResourcesService(List<CommonTitle> commonTitles) {
		doReturn(commonTitles).when(gitHubResourcesService)
				.getResource(FanDubSource.ANIMEDIA);
	}

	private void checkResult(CommonTitle expectedCommonTitle, Map<Integer, List<CommonTitle>> result) {
		List<CommonTitle> actualCommonTitleList = result.get(expectedCommonTitle.getMalId());
		assertEquals(1, actualCommonTitleList.size());
		CommonTitle actual = actualCommonTitleList.get(0);
		assertEquals(expectedCommonTitle, actual);
	}
}