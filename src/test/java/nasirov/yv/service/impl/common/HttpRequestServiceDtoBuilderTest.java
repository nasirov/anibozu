package nasirov.yv.service.impl.common;

import static nasirov.yv.utils.TestConstants.TEST_ACC_FOR_DEV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikPlayer;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikTitleEpisodes;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.jisedai.JisedaiTitleEpisodeDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.selenium_service.SeleniumServiceRequestDto;
import nasirov.yv.utils.TestConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * @author Nasirov Yuriy
 */
class HttpRequestServiceDtoBuilderTest extends AbstractTest {

	@Test
	void shouldBuildHttpRequestServiceDtoForMalService() {
		//given
		String url = externalServicesProps.getMalServiceUrl() + "titles?username=" + TEST_ACC_FOR_DEV + "&status=WATCHING";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getMalServiceBasicAuth());
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		MalServiceResponseDto fallback = MalServiceResponseDto.builder()
				.username(TEST_ACC_FOR_DEV)
				.malTitles(Collections.emptyList())
				.errorMessage(BaseConstants.GENERIC_ERROR_MESSAGE)
				.build();
		//when
		HttpRequestServiceDto<MalServiceResponseDto> result = httpRequestServiceDtoBuilder.malService(TEST_ACC_FOR_DEV, MalTitleWatchingStatus.WATCHING);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForFandubTitlesService() {
		//given
		String url = externalServicesProps.getFandubTitlesServiceUrl() + "titles?fanDubSources=ANIMEDIA&malId=42&malEpisodeId=1";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getFandubTitlesServiceBasicAuth());
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		Map<FanDubSource, List<CommonTitle>> fallback = Map.of(FanDubSource.ANIMEDIA, Collections.emptyList());
		//when
		HttpRequestServiceDto<Map<FanDubSource, List<CommonTitle>>> result =
				httpRequestServiceDtoBuilder.fandubTitlesService(List.of(FanDubSource.ANIMEDIA),
				42,
				1);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForSeleniumService() {
		//given
		String url = externalServicesProps.getSeleniumServiceUrl() + "content?url=https://foo.bar&timeoutInSec=5&cssSelector=a";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, externalServicesProps.getSeleniumServiceBasicAuth());
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		String fallback = StringUtils.EMPTY;
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.seleniumService(SeleniumServiceRequestDto.builder()
				.url("https://foo.bar")
				.timeoutInSec(5)
				.cssSelector("a")
				.build());
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnidub() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.ANIDUB) + TestConstants.REGULAR_TITLE_ANIDUB_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_ANIDUB_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.anidub(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnilibria() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.ANILIBRIA) + TestConstants.REGULAR_TITLE_ANILIBRIA_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_ANILIBRIA_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.anilibria(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnimedia() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.ANIMEDIA) + "embeds/playlist-j.txt/11/2";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		List<AnimediaEpisode> fallback = Collections.emptyList();
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_ANIMEDIA_URL);
		//when
		HttpRequestServiceDto<List<AnimediaEpisode>> result = httpRequestServiceDtoBuilder.animedia(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnimepik() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.ANIMEPIK) + "api/v1/" + TestConstants.REGULAR_TITLE_ANIMEPIK_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		AnimepikTitleEpisodes fallback = AnimepikTitleEpisodes.builder()
				.animepikPlayer(AnimepikPlayer.builder()
						.episodes(Collections.emptyList())
						.build())
				.build();
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_ANIMEPIK_URL);
		//when
		HttpRequestServiceDto<AnimepikTitleEpisodes> result = httpRequestServiceDtoBuilder.animepik(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForJisedai() {
		//given
		String url = fanDubProps.getJisedaiApiUrl() + "api/v1/anime/11/episode";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		List<JisedaiTitleEpisodeDto> fallback = Collections.emptyList();
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_JISEDAI_URL);
		//when
		HttpRequestServiceDto<List<JisedaiTitleEpisodeDto>> result = httpRequestServiceDtoBuilder.jisedai(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForJutsu() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.JUTSU) + TestConstants.REGULAR_TITLE_JUTSU_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_JUTSU_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.jutsu(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForNineAnime() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.NINEANIME) + "ajax/anime/servers?id=11";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_NINE_ANIME_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.nineAnime(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForShizaProject() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.SHIZAPROJECT) + TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_SHIZA_PROJECT_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.shizaProject(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForSovetRomantica() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.SOVETROMANTICA) + TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomantica(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForSovetRomanticaWithCookie() {
		//given
		String url = fanDubProps.getUrls()
				.get(FanDubSource.SOVETROMANTICA) + TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL;
		HttpMethod method = HttpMethod.GET;
		String cookie = "foobar";
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.COOKIE, cookie);
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle(TestConstants.REGULAR_TITLE_SOVET_ROMANTICA_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomantica(commonTitle, cookie);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}


	@Test
	void shouldBuildHttpRequestServiceDtoForSovetRomanticaDdosGuard() {
		//given
		String url = fanDubProps.getSovetRomanticaDdosGuardUrl() + "check.js";
		HttpMethod method = HttpMethod.GET;
		String referer = TestConstants.SOVET_ROMANTICA_URL;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.REFERER, referer);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		String fallback = StringUtils.EMPTY;
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomanticaDdosGuard();
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	private <T> void checkResult(HttpRequestServiceDto<T> result, String url, HttpMethod method, Map<String, String> headers,
			Set<Integer> retryableStatusCodes, T fallback) {
		assertEquals(url, result.getUrl());
		assertEquals(method, result.getMethod());
		assertEquals(headers, result.getHeaders());
		assertEquals(retryableStatusCodes, result.getRetryableStatusCodes());
		assertNull(result.getRequestBody());
		assertNotNull(result.getClientResponseFunction());
		assertEquals(fallback, result.getFallback());
		assertNotNull(result.getClientResponseFunction()
				.apply(ClientResponse.create(HttpStatus.OK)
						.build()));
	}

	private CommonTitle getCommonTitle(String titleUrl) {
		return CommonTitle.builder()
				.url(titleUrl)
				.id("11")
				.malId(42)
				.dataList(2)
				.build();
	}
}