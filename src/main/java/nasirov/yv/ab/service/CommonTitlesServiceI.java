package nasirov.yv.ab.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface CommonTitlesServiceI {

	Mono<Map<FandubSource, Map<Integer, List<CommonTitle>>>> getCommonTitlesMappedByMalId();

	Mono<Map<Integer, Map<FandubSource, List<CommonTitle>>>> getCommonTitles(Set<FandubSource> fandubSources, List<MalTitle> malTitles);
}
