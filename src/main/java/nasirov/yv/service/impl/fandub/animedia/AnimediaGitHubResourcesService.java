package nasirov.yv.service.impl.fandub.animedia;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
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
	public Map<Integer, Set<TitleReference>> getTitleReferences() {
		log.debug("Trying to convert Set<TitleReference> from GitHub to Map<distinct && non null - Integer titleIdOnMal, Set<TitleReference>>");
		Set<TitleReference> references = getResourceFromGitHub();
		List<Integer> malIds = extractDistinctTitleIdOnMalList(references);
		Map<Integer, Set<TitleReference>> result = convertToMap(references, malIds);
		log.debug("Got Map<distinct && non null - Integer titleIdOnMal, Set<TitleReference>> with size [{}]", result.size());
		return result;
	}

	private Set<TitleReference> getResourceFromGitHub() {
		return gitHubResourcesService.getResource(gitHubResourceProps.getAnimediaTitles(), TitleReference.class);
	}

	private List<Integer> extractDistinctTitleIdOnMalList(Set<TitleReference> references) {
		return references.stream()
				.map(TitleReference::getTitleIdOnMAL)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}

	private Map<Integer, Set<TitleReference>> convertToMap(Set<TitleReference> references, List<Integer> malIds) {
		return malIds.stream()
				.collect(Collectors.toMap(Function.identity(), x -> extractTitleReferencesByTitleIdOnMal(references, x)));
	}

	private Set<TitleReference> extractTitleReferencesByTitleIdOnMal(Set<TitleReference> references, Integer titleIdOnMal) {
		return references.stream()
				.filter(x -> titleIdOnMal.equals(x.getTitleIdOnMAL()))
				.collect(Collectors.toSet());
	}
}
