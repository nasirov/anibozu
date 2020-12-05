package nasirov.yv.service.impl.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class MalServiceTest {

	@Mock
	private HttpRequestServiceI httpRequestService;

	@Mock
	private HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@InjectMocks
	private MalService malService;

	@Test
	public void shouldReturnMalServiceResponseDtoMono() {
		//given
		UserInputDto userInputDto = UserInputDto.builder()
				.username("foobar")
				.build();
		HttpRequestServiceDto<MalServiceResponseDto> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.malService("foobar", MalTitleWatchingStatus.WATCHING);
		MalServiceResponseDto malServiceResponseDto = mock(MalServiceResponseDto.class);
		doReturn(Mono.just(malServiceResponseDto)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
		//when
		Mono<MalServiceResponseDto> result = malService.getUserWatchingTitles(userInputDto);
		//then
		assertEquals(malServiceResponseDto, result.block());
	}
}