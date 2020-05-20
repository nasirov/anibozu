package nasirov.yv.service.impl.fandub.jisedai;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.JisedaiGitHubResourcesServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JisedaiGitHubResourcesService implements JisedaiGitHubResourcesServiceI<JisedaiSiteTitle> {

	private final GitHubResourcesServiceI gitHubResourcesService;

	private final GitHubResourceProps gitHubResourceProps;

	@Override
	@Cacheable(value = "github", key = "'jisedaiTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, JisedaiSiteTitle> getJisedaiTitles() {
		log.debug("Trying to convert Set<JisedaiSiteTitle> from GitHub to Map<distinct && non null - Integer titleIdOnMal, JisedaiSiteTitle>");
		Set<JisedaiSiteTitle> jisedaiSiteTitles = getResourceFromGitHub();
		Map<Integer, JisedaiSiteTitle> result = convertToMap(jisedaiSiteTitles);
		log.debug("Got Map<distinct && non null - Integer titleIdOnMal, JisedaiSiteTitle> with size [{}]", result.size());
		return result;
	}

	private Set<JisedaiSiteTitle> getResourceFromGitHub() {
		return gitHubResourcesService.getResource(gitHubResourceProps.getJisedaiSiteTitles(), JisedaiSiteTitle.class);
	}

	private Map<Integer, JisedaiSiteTitle> convertToMap(Set<JisedaiSiteTitle> jisedaiSiteTitles) {
		return jisedaiSiteTitles.stream()
				.filter(this::isTitleIdOnMalNonNull)
				.collect(Collectors.toMap(JisedaiSiteTitle::getTitleIdOnMal, Function.identity(), (o, n) -> o));
	}

	private boolean isTitleIdOnMalNonNull(JisedaiSiteTitle title) {
		return Objects.nonNull(title.getTitleIdOnMal());
	}
}
