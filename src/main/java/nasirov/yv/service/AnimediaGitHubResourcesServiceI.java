package nasirov.yv.service;

import java.util.Map;
import java.util.Set;
import nasirov.yv.data.animedia.TitleReference;

/**
 * Created by nasirov.yv
 */
public interface AnimediaGitHubResourcesServiceI {

	Map<Integer, Set<TitleReference>> getTitleReferences();
}
