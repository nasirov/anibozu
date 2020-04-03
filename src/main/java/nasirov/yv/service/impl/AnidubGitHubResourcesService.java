package nasirov.yv.service.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.AnidubTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.AnidubGitHubResourcesServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnidubGitHubResourcesService implements AnidubGitHubResourcesServiceI {

	private final GitHubResourcesServiceI gitHubResourcesService;

	private final GitHubResourceProps gitHubResourceProps;

	@Override
	@Cacheable(value = "github", key = "'anidubTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, AnidubTitle> getAnidubTitles() {
		log.debug("Trying to convert Set<AnidubTitle> from GitHub to Map<distinct && non null - Integer titleIdOnMal, AnidubTitle>");
		Set<AnidubTitle> anidubTitles = getResourceFromGitHub();
		Map<Integer, AnidubTitle> result = convertToMap(anidubTitles);
		log.debug("Got Map<distinct && non null - Integer titleIdOnMal, AnidubTitle> with size [{}]", result.size());
		return result;
	}

	private Set<AnidubTitle> getResourceFromGitHub() {
		return gitHubResourcesService.getResource(gitHubResourceProps.getAnidubTitles(), AnidubTitle.class);
	}

	private Map<Integer, AnidubTitle> convertToMap(Set<AnidubTitle> anidubTitles) {
		return anidubTitles.stream()
				.filter(this::isTitleIdOnMalNonNull)
				.collect(Collectors.toMap(AnidubTitle::getTitleIdOnMal, Function.identity(), (o, n) -> o));
	}

	private boolean isTitleIdOnMalNonNull(AnidubTitle x) {
		return Objects.nonNull(x.getTitleIdOnMal());
	}
}
