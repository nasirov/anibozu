package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.AnythingGroupServiceI;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
public class AnythingGroupEpisodeNameAndUrlService extends AbstractEpisodeNameAndUrlService {

	private final AnythingGroupServiceI<Mono<List<CommonEpisode>>> reactiveAnythingGroupService;

	public AnythingGroupEpisodeNameAndUrlService(FanDubProps fanDubProps, CommonProps commonProps, HttpRequestServiceI httpRequestService,
			HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder, AnythingGroupServiceI<Mono<List<CommonEpisode>>> reactiveAnythingGroupService) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.ANYTHING_GROUP);
		this.reactiveAnythingGroupService = reactiveAnythingGroupService;
	}

	@Override
	protected Mono<List<CommonEpisode>> getEpisodes(CommonTitle commonTitle) {
		return reactiveAnythingGroupService.getTitleEpisodes(commonTitle.getUrl());
	}
}
