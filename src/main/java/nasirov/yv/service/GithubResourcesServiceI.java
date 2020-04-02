package nasirov.yv.service;

import java.util.Set;

/**
 * Created by nasirov.yv
 */
public interface GithubResourcesServiceI {

	<T> Set<T> getResource(String resourceName, Class<T> targetClass);
}
