package nasirov.yv.service.impl.fandub.jisedai;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.fandub.jisedai.JisedaiTitle;
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
public class JisedaiTitleService extends BaseTitlesService<JisedaiTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public JisedaiTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'jisedaiTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<JisedaiTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<JisedaiTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getJisedaiTitles(), JisedaiTitle.class);
	}
}
