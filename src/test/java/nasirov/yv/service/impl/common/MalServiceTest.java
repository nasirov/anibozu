package nasirov.yv.service.impl.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
class MalServiceTest extends AbstractTest {

	@Test
	void shouldReturnMalServiceResponseDtoMono() {
		//given
		UserInputDto userInputDto = buildUserInputDto();
		MalTitle regularTitle = buildRegularTitle();
		MalServiceResponseDto malServiceResponseDto = buildMalServiceResponseDto(Lists.newArrayList(regularTitle), "");
		mockExternalMalServiceResponse(malServiceResponseDto);
		//when
		Mono<MalServiceResponseDto> result = malService.getUserWatchingTitles(userInputDto);
		//then
		assertEquals(malServiceResponseDto, result.block());
	}
}