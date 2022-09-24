package nasirov.yv.ac.service.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nasirov.yv.ac.data.constants.BaseConstants;
import nasirov.yv.ac.service.HttpRequestServiceDtoBuilderI;
import nasirov.yv.ac.util.MalUtils;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.fandub_titles_service.FandubTitlesServiceRequestDto;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.starter.common.properties.ExternalServicesProperties;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Service
@RequiredArgsConstructor
public class HttpRequestServiceDtoBuilder implements HttpRequestServiceDtoBuilderI {

	private final StarterCommonProperties starterCommonProperties;

	@Override
	public HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status) {
		ExternalServicesProperties externalServicesProperties = starterCommonProperties.getExternalServices();
		return new HttpRequestServiceDto<>(
				externalServicesProperties.getMalServiceUrl() + "titles?username=" + username + "&status=" + status.name(),
				HttpMethod.GET, Collections.emptyMap(), null, Collections.emptySet(),
				y -> y.bodyToMono(MalServiceResponseDto.class),
				MalServiceResponseDto.builder()
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
		ExternalServicesProperties externalServicesProperties = starterCommonProperties.getExternalServices();
		return new HttpRequestServiceDto<>(externalServicesProperties.getFandubTitlesServiceUrl() + "titles", HttpMethod.POST,
				Collections.emptyMap(),
				FandubTitlesServiceRequestDto.builder().fandubSources(fandubSources).malIdToEpisode(malIdToEpisode).build(),
				Collections.emptySet(),
				x -> x.bodyToMono(new ParameterizedTypeReference<Map<Integer, Map<FandubSource, List<CommonTitle>>>>() {}),
				malIdToEpisode.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey,
								x -> fandubSources.stream().collect(Collectors.toMap(Function.identity(), y -> Collections.emptyList())))));
	}
}
