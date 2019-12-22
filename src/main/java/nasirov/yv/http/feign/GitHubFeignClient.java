package nasirov.yv.http.feign;

import java.util.Set;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.http.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "github-feign-client", configuration = FeignClientConfig.class)
@RequestMapping(value = "/nasirov/anime-checker-resources/master", headers = {"Accept-Encoding=gzip, deflate, br"})
public interface GitHubFeignClient {

	@GetMapping(value = "/animediaSearchList.json", produces = "text/plain; charset=utf-8")
	ResponseEntity<Set<AnimediaSearchListTitle>> getAnimediaSearchList();

	@GetMapping(value = "/references.json", produces = "text/plain; charset=utf-8")
	ResponseEntity<Set<TitleReference>> getReferences();
}
