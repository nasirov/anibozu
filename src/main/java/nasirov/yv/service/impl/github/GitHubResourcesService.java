package nasirov.yv.service.impl.github;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.feign.github.GitHubFeignClient;
import nasirov.yv.fandub.service.spring.boot.starter.service.WrappedObjectMapperI;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubResourcesService implements GitHubResourcesServiceI {

	private final GitHubFeignClient gitHubFeignClient;

	private final GitHubAuthProps gitHubAuthProps;

	private final WrappedObjectMapperI wrappedObjectMapper;

	/**
	 * Searches for a resource from GitHub
	 *
	 * @param resourceName a github resource name from {@link GitHubResourceProps}
	 * @return list with dto
	 */
	@Override
	public List<CommonTitle> getResource(String resourceName) {
		log.debug("Trying to get resource [{}] from GitHub...", resourceName);
		String resourceString = gitHubFeignClient.getResource("token " + gitHubAuthProps.getToken(), resourceName);
		List<CommonTitle> result = wrappedObjectMapper.unmarshalToList(resourceString, CommonTitle.class, ArrayList.class);
		log.debug("Got result with size [{}] from GitHub.", result.size());
		return result;
	}
}
