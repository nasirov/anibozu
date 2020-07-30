package nasirov.yv.service;

import java.util.List;
import nasirov.yv.fandub.dto.fandub.common.CommonTitle;


/**
 * Created by nasirov.yv
 */
public interface GitHubResourcesServiceI {

	List<CommonTitle> getResource(String resourceName);
}
