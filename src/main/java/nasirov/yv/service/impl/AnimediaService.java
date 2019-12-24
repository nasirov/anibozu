package nasirov.yv.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.springframework.web.util.UriUtils.encodePath;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.AnimediaFeignClient;
import nasirov.yv.http.feign.GitHubFeignClient;
import nasirov.yv.service.AnimediaServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaService implements AnimediaServiceI {

	private final AnimediaFeignClient animediaFeignClient;

	private final GitHubFeignClient gitHubFeignClient;

	private final UrlsNames urlsNames;

	/**
	 * Searches for fresh animedia search list from animedia
	 *
	 * @return the list with title search info on animedia
	 */
	@Override
	@Cacheable(value = "animedia", key = "'aslFromAnimedia'", unless = "#result?.isEmpty()")
	public Set<AnimediaSearchListTitle> getAnimediaSearchListFromAnimedia() {
		ResponseEntity<Set<AnimediaSearchListTitle>> animediaRespone = animediaFeignClient.getAnimediaSearchList();
		return ofNullable(animediaRespone.getBody()).orElseGet(Collections::emptySet)
				.stream()
				.map(this::changeUrl)
				.collect(Collectors.toSet());
	}

	/**
	 * Searches for fresh animedia search list from github
	 *
	 * @return the list with title search info on animedia
	 */
	@Override
	@Cacheable(value = "github", key = "'aslFromGitHub'", unless = "#result?.isEmpty()")
	public Set<AnimediaSearchListTitle> getAnimediaSearchListFromGitHub() {
		ResponseEntity<Set<AnimediaSearchListTitle>> animediaResponse = gitHubFeignClient.getAnimediaSearchList();
		return ofNullable(animediaResponse.getBody()).orElseGet(Collections::emptySet);
	}

	private AnimediaSearchListTitle changeUrl(AnimediaSearchListTitle animediaSearchListTitle) {
		animediaSearchListTitle.setUrl(encodePath(animediaSearchListTitle.getUrl()
				.replaceAll(urlsNames.getAnimediaUrls()
						.getOnlineAnimediaTv(), ""), UTF_8));
		return animediaSearchListTitle;
	}
}
