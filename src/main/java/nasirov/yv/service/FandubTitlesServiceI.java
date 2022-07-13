package nasirov.yv.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface FandubTitlesServiceI {

	Mono<Map<Integer, Map<FandubSource, List<CommonTitle>>>> getCommonTitles(Set<FandubSource> fandubSources,
			List<MalTitle> malTitles);
}