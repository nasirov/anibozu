package nasirov.yv.service.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub_titles_service.FandubTitlesServiceRequestDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.util.MalUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Service
@RequiredArgsConstructor
public class HttpRequestServiceDtoBuilder implements HttpRequestServiceDtoBuilderI {

	private final ExternalServicesProps externalServicesProps;

	@Override
	public HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status) {
		return new HttpRequestServiceDto<>(
				externalServicesProps.getMalServiceUrl() + "titles?username=" + username + "&status=" + status.name(),
				HttpMethod.GET, Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getMalServiceBasicAuth()),
				null, Collections.emptySet(), y -> y.bodyToMono(MalServiceResponseDto.class), MalServiceResponseDto.builder()
				.username(username)
				.malTitles(Collections.emptyList())
				.errorMessage(BaseConstants.GENERIC_ERROR_MESSAGE)
				.build());
	}

	@Override
	public HttpRequestServiceDto<Map<Integer, Map<FandubSource, List<CommonTitle>>>> fandubTitlesService(
			Set<FandubSource> fandubSources, List<MalTitle> watchingTitles) {
		Map<Integer, Integer> malIdToEpisode = watchingTitles.stream()
				.collect(Collectors.toMap(MalTitle::getId, MalUtils::getNextEpisodeForWatch, (o, n) -> o, LinkedHashMap::new));
		return new HttpRequestServiceDto<>(externalServicesProps.getFandubTitlesServiceUrl() + "titles", HttpMethod.POST,
				Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getFandubTitlesServiceBasicAuth()),
				FandubTitlesServiceRequestDto.builder().fandubSources(fandubSources).malIdToEpisode(malIdToEpisode).build(),
				Collections.emptySet(),
				x -> x.bodyToMono(new ParameterizedTypeReference<Map<Integer, Map<FandubSource, List<CommonTitle>>>>() {}),
				malIdToEpisode.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey,
								x -> fandubSources.stream().collect(Collectors.toMap(Function.identity(), y -> Collections.emptyList())))));
	}
}
