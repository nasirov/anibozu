package nasirov.yv.service.impl.fandub.animedia;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.AnimediaGitHubResourcesServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaGitHubResourcesService implements AnimediaGitHubResourcesServiceI {

	private final GitHubResourcesServiceI gitHubResourcesService;

	private final GitHubResourceProps gitHubResourceProps;

	@Override
	@Cacheable(value = "github", key = "'animediaTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, Set<AnimediaTitle>> getAnimediaTitles() {
		log.debug("Trying to convert Set<AnimediaTitle> from GitHub to Map<distinct && non null - Integer titleIdOnMal, Set<AnimediaTitle>>");
		Set<AnimediaTitle> animediaTitles = getResourceFromGitHub();
		List<Integer> malIds = extractDistinctTitleIdOnMalList(animediaTitles);
		Map<Integer, Set<AnimediaTitle>> result = convertToMap(animediaTitles, malIds);
		log.debug("Got Map<distinct && non null - Integer titleIdOnMal, Set<AnimediaTitle>> with size [{}]", result.size());
		return result;
	}

	private Set<AnimediaTitle> getResourceFromGitHub() {
		return gitHubResourcesService.getResource(gitHubResourceProps.getAnimediaTitles(), AnimediaTitle.class);
	}

	private List<Integer> extractDistinctTitleIdOnMalList(Set<AnimediaTitle> animediaTitles) {
		return animediaTitles.stream()
				.map(AnimediaTitle::getTitleIdOnMAL)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}

	private Map<Integer, Set<AnimediaTitle>> convertToMap(Set<AnimediaTitle> animediaTitles, List<Integer> malIds) {
		return malIds.stream()
				.collect(Collectors.toMap(Function.identity(), x -> extractAnimediaTitlesByTitleIdOnMal(animediaTitles, x)));
	}

	private Set<AnimediaTitle> extractAnimediaTitlesByTitleIdOnMal(Set<AnimediaTitle> animediaTitles, Integer titleIdOnMal) {
		return animediaTitles.stream()
				.filter(x -> titleIdOnMal.equals(x.getTitleIdOnMAL()))
				.collect(Collectors.toSet());
	}
}
