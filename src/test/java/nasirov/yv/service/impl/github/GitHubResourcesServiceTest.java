package nasirov.yv.service.impl.github;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.github.GitHubFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.service.WrappedObjectMapperI;
import nasirov.yv.fandub.service.spring.boot.starter.service.impl.WrappedObjectMapper;
import nasirov.yv.utils.CommonTitleTestBuilder;
import nasirov.yv.utils.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubResourcesServiceTest {

	@Mock
	private GitHubFeignClient gitHubFeignClient;

	@Mock
	private GitHubAuthProps gitHubAuthProps;

	@Spy
	private WrappedObjectMapperI wrappedObjectMapper = new WrappedObjectMapper(new ObjectMapper());

	@InjectMocks
	private GitHubResourcesService gitHubResourcesService;

	@Test
	public void shouldReturnAnimediaTitles() {
		//given
		mockToken();
		mockGetResource();
		List<CommonTitle> expected = CommonTitleTestBuilder.buildCommonTitles(FanDubSource.ANIMEDIA);
		//when
		List<CommonTitle> result = gitHubResourcesService.getResource("commonTitles.json");
		//then
		assertEquals(expected.size(), result.size());
		result.forEach(x -> assertTrue(expected.contains(x)));
	}

	private void mockToken() {
		doReturn("foobar").when(gitHubAuthProps)
				.getToken();
	}

	private void mockGetResource() {
		String fileName = "commonTitles.json";
		doReturn(IOUtils.readFromFile("classpath:__files/github/" + fileName)).when(gitHubFeignClient)
				.getResource("token foobar", fileName);
	}
}