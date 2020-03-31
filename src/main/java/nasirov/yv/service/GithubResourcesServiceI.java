package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.TitleReference;

/**
 * Created by nasirov.yv
 */
public interface GithubResourcesServiceI {

	<T> Set<T> getResource(String resourceName, Class<T> targetClass);

	void updateReferences(Set<TitleReference> references);
}
