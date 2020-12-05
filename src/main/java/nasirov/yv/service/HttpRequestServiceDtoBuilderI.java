package nasirov.yv.service;

import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;

/**
 * @author Nasirov Yuriy
 */
public interface HttpRequestServiceDtoBuilderI {

	HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status);

	HttpRequestServiceDto<List<CommonTitle>> fandubTitlesService(FanDubSource fanDubSource, int malId, int malEpisodeId);

	HttpRequestServiceDto<String> anidub(CommonTitle commonTitle);

	HttpRequestServiceDto<String> anilibria(CommonTitle commonTitle);

	HttpRequestServiceDto<List<AnimediaEpisode>> animedia(CommonTitle commonTitle);

	HttpRequestServiceDto<List<AnimepikEpisode>> animepik(CommonTitle commonTitle);

	HttpRequestServiceDto<String> jisedai(CommonTitle commonTitle);

	HttpRequestServiceDto<String> jutsu(CommonTitle commonTitle);

	HttpRequestServiceDto<String> nineAnime(CommonTitle commonTitle);

	HttpRequestServiceDto<String> shizaProject(CommonTitle commonTitle);

	HttpRequestServiceDto<String> sovetRomantica(CommonTitle commonTitle);
}
