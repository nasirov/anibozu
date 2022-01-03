package nasirov.yv.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikTitleEpisodes;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.jisedai.JisedaiTitleEpisodeDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.selenium_service.SeleniumServiceRequestDto;

/**
 * @author Nasirov Yuriy
 */
public interface HttpRequestServiceDtoBuilderI {

	HttpRequestServiceDto<MalServiceResponseDto> malService(String username, MalTitleWatchingStatus status);

	HttpRequestServiceDto<Map<Integer, Map<FanDubSource, List<CommonTitle>>>> fandubTitlesService(Set<FanDubSource> fanDubSources,
			List<MalTitle> watchingTitles);

	HttpRequestServiceDto<String> seleniumService(SeleniumServiceRequestDto seleniumServiceRequestDto);

	HttpRequestServiceDto<String> anidub(CommonTitle commonTitle);

	HttpRequestServiceDto<String> anilibria(CommonTitle commonTitle);

	HttpRequestServiceDto<List<AnimediaEpisode>> animedia(CommonTitle commonTitle);

	HttpRequestServiceDto<AnimepikTitleEpisodes> animepik(CommonTitle commonTitle);

	HttpRequestServiceDto<List<JisedaiTitleEpisodeDto>> jisedai(CommonTitle commonTitle);

	HttpRequestServiceDto<String> jutsu(CommonTitle commonTitle);

	HttpRequestServiceDto<String> nineAnime(CommonTitle commonTitle);

	HttpRequestServiceDto<String> shizaProject(CommonTitle commonTitle);

	HttpRequestServiceDto<String> sovetRomantica(CommonTitle commonTitle);

	HttpRequestServiceDto<String> sovetRomantica(CommonTitle commonTitle, String cookie);

	HttpRequestServiceDto<String> sovetRomanticaDdosGuard();
}
