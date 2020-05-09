package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.data.constants.ServiceSourceType.API;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.api.AnidubApiTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.AnidubGitHubResourcesServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.anidub-episode-url-service-source", havingValue = API)
public class AnidubApiGitHubResourcesService implements AnidubGitHubResourcesServiceI<AnidubApiTitle> {

	private final GitHubResourcesServiceI gitHubResourcesService;

	private final GitHubResourceProps gitHubResourceProps;

	@Override
	@Cacheable(value = "github", key = "'anidubTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, AnidubApiTitle> getAnidubTitles() {
		log.debug("Trying to convert Set<AnidubTitle> from GitHub to Map<distinct && non null - Integer titleIdOnMal, AnidubTitle>");
		Set<AnidubApiTitle> anidubApiTitles = getResourceFromGitHub();
		Map<Integer, AnidubApiTitle> result = convertToMap(anidubApiTitles);
		log.debug("Got Map<distinct && non null - Integer titleIdOnMal, AnidubTitle> with size [{}]", result.size());
		return result;
	}

	private Set<AnidubApiTitle> getResourceFromGitHub() {
		return gitHubResourcesService.getResource(gitHubResourceProps.getAnidubApiTitles(), AnidubApiTitle.class);
	}

	private Map<Integer, AnidubApiTitle> convertToMap(Set<AnidubApiTitle> anidubApiTitles) {
		return anidubApiTitles.stream()
				.filter(this::isTitleIdOnMalNonNull)
				.collect(Collectors.toMap(AnidubApiTitle::getTitleIdOnMal, Function.identity(), (o, n) -> o));
	}

	private boolean isTitleIdOnMalNonNull(AnidubApiTitle x) {
		return Objects.nonNull(x.getTitleIdOnMal());
	}
}
