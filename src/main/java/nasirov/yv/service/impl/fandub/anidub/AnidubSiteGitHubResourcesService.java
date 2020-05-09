package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.data.constants.ServiceSourceType.SITE;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.AnidubGitHubResourcesServiceI;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.anidub-episode-url-service-source", havingValue = SITE)
public class AnidubSiteGitHubResourcesService implements AnidubGitHubResourcesServiceI<AnidubSiteTitle> {

	private final GitHubResourcesServiceI gitHubResourcesService;

	private final GitHubResourceProps gitHubResourceProps;

	@Override
	@Cacheable(value = "github", key = "'anidubTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, AnidubSiteTitle> getAnidubTitles() {
		log.debug("Trying to convert Set<AnidubSiteTitle> from GitHub to Map<distinct && non null - Integer titleIdOnMal, AnidubSiteTitle>");
		Set<AnidubSiteTitle> anidubTitles = getResourceFromGitHub();
		Map<Integer, AnidubSiteTitle> result = convertToMap(anidubTitles);
		log.debug("Got Map<distinct && non null - Integer titleIdOnMal, AnidubSiteTitle> with size [{}]", result.size());
		return result;
	}

	private Set<AnidubSiteTitle> getResourceFromGitHub() {
		return gitHubResourcesService.getResource(gitHubResourceProps.getAnidubSiteTitles(), AnidubSiteTitle.class);
	}

	private Map<Integer, AnidubSiteTitle> convertToMap(Set<AnidubSiteTitle> anidubTitles) {
		return anidubTitles.stream()
				.filter(this::isTitleIdOnMalNonNull)
				.collect(Collectors.toMap(AnidubSiteTitle::getTitleIdOnMal, Function.identity(), (o, n) -> o));
	}

	private boolean isTitleIdOnMalNonNull(AnidubSiteTitle anidubSiteTitle) {
		return Objects.nonNull(anidubSiteTitle.getTitleIdOnMal());
	}
}
