package nasirov.yv.ac.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.common.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;

/**
 * @author Nasirov Yuriy
 */
public interface HttpRequestServiceDtoBuilderI {

	HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status);

	HttpRequestServiceDto<Map<Integer, Map<FandubSource, List<CommonTitle>>>> fandubTitlesService(
			Set<FandubSource> fandubSources, List<MalTitle> watchingTitles);
}
