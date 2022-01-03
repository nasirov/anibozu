package nasirov.yv.service;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.front.Anime;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface AnimeServiceI {

	/**
	 * Builds an {@link Anime} based on given watching title and fandub sources
	 *
	 * @param watchingTitle              user currently watching title
	 * @param commonTitlesByFandubSource common titles by fandub source
	 * @return an {@link Anime} dto wrapped with {@link Mono}
	 */
	Mono<Anime> buildAnime(MalTitle watchingTitle, Map<FanDubSource, List<CommonTitle>> commonTitlesByFandubSource);
}
