package nasirov.yv.service.impl.fandub.anidub;

import static nasirov.yv.data.constants.ServiceSourceType.SITE;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.impl.common.BaseTitlesService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@ConditionalOnProperty(name = "application.services.anidub-episode-url-service-source", havingValue = SITE)
public class AnidubSiteTitleService extends BaseTitlesService<AnidubSiteTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public AnidubSiteTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'anidubTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<AnidubSiteTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<AnidubSiteTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getAnidubSiteTitles(), AnidubSiteTitle.class);
	}
}
