package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.github.GitHubResource;


/**
 * Created by nasirov.yv
 */
public interface GitHubResourcesServiceI {

	<T extends GitHubResource> Set<T> getResource(String resourceName, Class<T> targetClass);
}
