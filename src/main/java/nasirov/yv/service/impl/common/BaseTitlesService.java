package nasirov.yv.service.impl.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.github.GitHubResource;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.TitlesServiceI;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nasirov.yv
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseTitlesService<T extends GitHubResource> implements TitlesServiceI<T> {

	private final GitHubResourcesServiceI gitHubResourcesService;

	@Override
	public Map<Integer, List<T>> getTitles() {
		Pair<String, Class<T>> resourceProperties = getTargetResourceProperties();
		Class<T> targetClass = resourceProperties.getValue();
		log.debug("Trying to convert List<{}> from GitHub to Map<non null - Integer titleIdOnMal, List<{}>>...", targetClass, targetClass);
		List<T> titles = getResourceFromGitHub(resourceProperties.getKey(), targetClass);
		Map<Integer, List<T>> result = convertToMap(titles);
		log.debug("Got Map<non null - Integer titleIdOnMal, List<{}>> with size [{}].", targetClass, result.size());
		return result;
	}

	protected abstract Pair<String, Class<T>> getTargetResourceProperties();

	private List<T> getResourceFromGitHub(String resourceName, Class<T> resourceClass) {
		return gitHubResourcesService.getResource(resourceName, resourceClass);
	}

	private Map<Integer, List<T>> convertToMap(List<T> titles) {
		return titles.stream()
				.filter(this::isTitleIdOnMalNonNull)
				.collect(Collectors.groupingBy(T::getTitleIdOnMal));
	}

	private boolean isTitleIdOnMalNonNull(T title) {
		return Objects.nonNull(title.getTitleIdOnMal());
	}
}
