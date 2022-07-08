package nasirov.yv.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;

/**
 * @author Nasirov Yuriy
 */
public interface HttpRequestServiceDtoBuilderI {

	HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status);

	HttpRequestServiceDto<Map<Integer, Map<FanDubSource, List<CommonTitle>>>> fandubTitlesService(
			Set<FanDubSource> fanDubSources, List<MalTitle> watchingTitles);
}
