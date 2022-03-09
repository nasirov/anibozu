package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.fandub.service.spring.boot.starter.service.JamClubServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
public class JamClubEpisodeNameAndUrlService extends AbstractEpisodeNameAndUrlService {

	private final JamClubServiceI<Mono<List<CommonEpisode>>> reactiveJamClubService;

	public JamClubEpisodeNameAndUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder, JamClubServiceI<Mono<List<CommonEpisode>>> reactiveJamClubService) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.JAMCLUB);
		this.reactiveJamClubService = reactiveJamClubService;
	}

	@Override
	protected Mono<List<CommonEpisode>> getEpisodes(CommonTitle commonTitle) {
		return reactiveJamClubService.getTitleEpisodes(commonTitle.getUrl(),
				commonTitle.getId()
						.getId());
	}
}
