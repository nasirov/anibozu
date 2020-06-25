package nasirov.yv.service.impl.fandub.anilibria;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.anilibria.AnilibriaTitle;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.service.GitHubResourcesServiceI;
import nasirov.yv.service.impl.fandub.BaseTitlesService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
public class AnilibriaTitleService extends BaseTitlesService<AnilibriaTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public AnilibriaTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'anilibriaTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<AnilibriaTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<AnilibriaTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getAnilibriaTitles(), AnilibriaTitle.class);
	}
}
