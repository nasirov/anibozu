package nasirov.yv.service.impl.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.fandub.dto.constant.FanDubSource;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.service.GitHubResourcesServiceI;
import nasirov.yv.service.TitlesServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TitlesService implements TitlesServiceI {

	private final GitHubResourcesServiceI gitHubResourcesService;

	@Override
	@Cacheable(value = "github", key = "#fanDubSource.name()", unless = "#result == null || #result.isEmpty()")
	public Map<Integer, List<CommonTitle>> getTitles(FanDubSource fanDubSource) {
		log.debug("Trying to convert List<CommonTitle> from GitHub to Map<Integer, List<CommonTitle>>...");
		List<CommonTitle> titles = gitHubResourcesService.getResource(fanDubSource);
		Map<Integer, List<CommonTitle>> result = convertToMap(titles);
		log.debug("Got Map<Integer, List<CommonTitle>> with size [{}].", result.size());
		return result;
	}

	private Map<Integer, List<CommonTitle>> convertToMap(List<CommonTitle> titles) {
		return titles.stream()
				.filter(this::isTitleFoundOnMal)
				.collect(Collectors.groupingBy(CommonTitle::getMalId));
	}

	private boolean isTitleFoundOnMal(CommonTitle title) {
		return Objects.nonNull(title.getMalId());
	}
}
