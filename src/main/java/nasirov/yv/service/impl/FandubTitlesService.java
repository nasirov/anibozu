package nasirov.yv.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.FandubTitlesServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
@RequiredArgsConstructor
public class FandubTitlesService implements FandubTitlesServiceI {

	private final HttpRequestServiceI httpRequestService;

	private final HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder;

	@Override
	public Mono<Map<Integer, Map<FanDubSource, List<CommonTitle>>>> getCommonTitles(Set<FanDubSource> fanDubSources,
			List<MalTitle> malTitles) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.fandubTitlesService(fanDubSources,
				malTitles));
	}
}
