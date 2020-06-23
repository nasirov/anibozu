package nasirov.yv.service.impl.fandub.anilibria.site;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.anilibria.site.AnilibriaSiteTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.impl.common.BaseTitlesService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class AnilibriaSiteTitleService extends BaseTitlesService<AnilibriaSiteTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public AnilibriaSiteTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'anilibriaTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<AnilibriaSiteTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<AnilibriaSiteTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getAnilibriaSiteTitles(), AnilibriaSiteTitle.class);
	}
}
