package nasirov.yv.service.impl.fandub.jisedai.site;

import java.util.List;
import java.util.Map;
import nasirov.yv.data.jisedai.site.JisedaiSiteTitle;
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
public class JisedaiSiteTitleService extends BaseTitlesService<JisedaiSiteTitle> {

	private final GitHubResourceProps gitHubResourceProps;

	public JisedaiSiteTitleService(GitHubResourcesServiceI gitHubResourcesService, GitHubResourceProps gitHubResourceProps) {
		super(gitHubResourcesService);
		this.gitHubResourceProps = gitHubResourceProps;
	}

	@Override
	@Cacheable(value = "github", key = "'jisedaiTitles'", unless = "#result?.isEmpty()")
	public Map<Integer, List<JisedaiSiteTitle>> getTitles() {
		return super.getTitles();
	}

	@Override
	protected Pair<String, Class<JisedaiSiteTitle>> getTargetResourceProperties() {
		return Pair.of(gitHubResourceProps.getJisedaiSiteTitles(), JisedaiSiteTitle.class);
	}
}
