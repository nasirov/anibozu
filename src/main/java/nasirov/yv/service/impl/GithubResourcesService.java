package nasirov.yv.service.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.http.feign.GitHubFeignClient;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.service.GithubResourcesServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GithubResourcesService implements GithubResourcesServiceI {

	private final GitHubFeignClient gitHubFeignClient;

	private final GitHubAuthProps gitHubAuthProps;

	private final WrappedObjectMapperI wrappedObjectMapper;

	/**
	 * Searches for references which extracted to GitHub
	 *
	 * @return animedia references
	 */
	@Override
	@Cacheable(value = "github", key = "#resourceName", unless = "#result?.isEmpty()")
	public <T> Set<T> getResource(String resourceName, Class<T> targetClass) {
		String resourceString = gitHubFeignClient.getResource("token " + gitHubAuthProps.getToken(), resourceName);
		return wrappedObjectMapper.unmarshal(resourceString, targetClass, LinkedHashSet.class);
	}
}
