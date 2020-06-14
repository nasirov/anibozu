package nasirov.yv.service.impl.fandub.animepik;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.anime_pik.api.AnimepikTitle;
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
public class AnimepikTitleService extends BaseTitlesService<AnimepikTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public AnimepikTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'animepikTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<AnimepikTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<AnimepikTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getAnimepikTitles(), AnimepikTitle.class);
	}
}
