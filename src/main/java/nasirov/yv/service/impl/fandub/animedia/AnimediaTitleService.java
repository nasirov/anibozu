package nasirov.yv.service.impl.fandub.animedia;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.animedia.AnimediaTitle;
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
public class AnimediaTitleService extends BaseTitlesService<AnimediaTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public AnimediaTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'animediaTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<AnimediaTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<AnimediaTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getAnimediaTitles(), AnimediaTitle.class);
	}
}
