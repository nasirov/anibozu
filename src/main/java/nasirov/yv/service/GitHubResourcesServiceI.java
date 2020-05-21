package nasirov.yv.service;

import java.util.List;
import nasirov.yv.data.github.GitHubResource;


/**
 * Created by nasirov.yv
 */
public interface GitHubResourcesServiceI {

	<T extends GitHubResource> List<T> getResource(String resourceName, Class<T> targetClass);
}
