package nasirov.yv.ab.service;

import java.util.List;
import java.util.Map;
import nasirov.yv.ab.dto.internal.GithubCacheKey;
import nasirov.yv.starter.common.dto.fandub.common.CommonEpisode;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface CommonTitlesServiceI {

	Mono<Map<GithubCacheKey, List<CommonEpisode>>> getCommonEpisodesMappedByKey();
}
