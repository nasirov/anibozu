package nasirov.yv.service.impl.fandub.anidub;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.anidub.AnidubTitle;
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
public class AnidubTitleService extends BaseTitlesService<AnidubTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public AnidubTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'anidubTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<AnidubTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<AnidubTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getAnidubTitles(), AnidubTitle.class);
	}
}
