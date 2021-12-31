package nasirov.yv.service.impl.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.utils.TestConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * @author Nasirov Yuriy
 */
@ExtendWith(MockitoExtension.class)
class HttpRequestServiceDtoBuilderTest {

	private static final String FANDUB_TITLES_SERVICE_BASIC_AUTH = "Basic foo";

	private static final String MAL_SERVICE_BASIC_AUTH = "Basic bar";

	private static final String SELENIUM_SERVICE_BASIC_AUTH = "Basic baz";

	private static final String FANDUB_TITLES_SERVICE_URL = "http://fandub-titles-serivce.foo/";

	private static final String MAL_SERVICE_URL = "http://mal-service.foo/";

	private static final String SELENIUM_SERVICE_URL = "http://selenium-service.foo/";

	private static final String TITLE_URL = "title-url";

	@Mock
	private FanDubProps fanDubProps;

	@Mock
	private ExternalServicesProps externalServicesProps;

	@InjectMocks
	private HttpRequestServiceDtoBuilder httpRequestServiceDtoBuilder;

	@Test
	void shouldBuildHttpRequestServiceDtoForMalService() {
		//given
		String url = MAL_SERVICE_URL + "titles?username=foobar&status=WATCHING";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, MAL_SERVICE_BASIC_AUTH);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		MalServiceResponseDto fallback = MalServiceResponseDto.builder()
				.username("foobar")
				.malTitles(Collections.emptyList())
				.errorMessage(BaseConstants.GENERIC_ERROR_MESSAGE)
				.build();
		doReturn(MAL_SERVICE_BASIC_AUTH).when(externalServicesProps)
				.getMalServiceBasicAuth();
		doReturn(MAL_SERVICE_URL).when(externalServicesProps)
				.getMalServiceUrl();
		//when
		HttpRequestServiceDto<MalServiceResponseDto> result = httpRequestServiceDtoBuilder.malService("foobar", MalTitleWatchingStatus.WATCHING);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForFandubTitlesService() {
		//given
		String url = FANDUB_TITLES_SERVICE_URL + "titles?fanDubSources=ANIMEDIA&malId=42&malEpisodeId=1";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, FANDUB_TITLES_SERVICE_BASIC_AUTH);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		Map<FanDubSource, List<CommonTitle>> fallback = Map.of(FanDubSource.ANIMEDIA, Collections.emptyList());
		doReturn(FANDUB_TITLES_SERVICE_BASIC_AUTH).when(externalServicesProps)
				.getFandubTitlesServiceBasicAuth();
		doReturn(FANDUB_TITLES_SERVICE_URL).when(externalServicesProps)
				.getFandubTitlesServiceUrl();
		//when
		HttpRequestServiceDto<Map<FanDubSource, List<CommonTitle>>> result = httpRequestServiceDtoBuilder.fandubTitlesService(List.of(FanDubSource.ANIMEDIA), 42, 1);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForSeleniumService() {
		//given
		String url = SELENIUM_SERVICE_URL + "content?url=https://foo.bar&timeoutInSec=5&cssSelector=a";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, SELENIUM_SERVICE_BASIC_AUTH);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		String fallback = StringUtils.EMPTY;
		doReturn(SELENIUM_SERVICE_BASIC_AUTH).when(externalServicesProps)
				.getSeleniumServiceBasicAuth();
		doReturn(SELENIUM_SERVICE_URL).when(externalServicesProps)
				.getSeleniumServiceUrl();
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
		String url = TestConstants.ANIDUB_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.ANIDUB, TestConstants.ANIDUB_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.anidub(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnilibria() {
		//given
		String url = TestConstants.ANILIBRIA_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.ANILIBRIA, TestConstants.ANILIBRIA_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.anilibria(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnimedia() {
		//given
		String url = TestConstants.ANIMEDIA_ONLINE_TV + "embeds/playlist-j.txt/11/2";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		List<AnimediaEpisode> fallback = Collections.emptyList();
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.ANIMEDIA, TestConstants.ANIMEDIA_ONLINE_TV);
		//when
		HttpRequestServiceDto<List<AnimediaEpisode>> result = httpRequestServiceDtoBuilder.animedia(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForAnimepik() {
		//given
		String url = TestConstants.ANIMEPIK_URL + "api/v1/" + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		AnimepikTitleEpisodes fallback = AnimepikTitleEpisodes.builder()
				.animepikPlayer(AnimepikPlayer.builder()
						.episodes(Collections.emptyList())
						.build())
				.build();
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.ANIMEPIK, TestConstants.ANIMEPIK_URL);
		//when
		HttpRequestServiceDto<AnimepikTitleEpisodes> result = httpRequestServiceDtoBuilder.animepik(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForJisedai() {
		//given
		String url = TestConstants.JISEDAI_API_URL + "api/v1/anime/11/episode";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		List<JisedaiTitleEpisodeDto> fallback = Collections.emptyList();
		CommonTitle commonTitle = getCommonTitle();
		doReturn(TestConstants.JISEDAI_API_URL).when(fanDubProps)
				.getJisedaiApiUrl();
		//when
		HttpRequestServiceDto<List<JisedaiTitleEpisodeDto>> result = httpRequestServiceDtoBuilder.jisedai(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForJutsu() {
		//given
		String url = TestConstants.JUTSU_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.JUTSU, TestConstants.JUTSU_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.jutsu(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForNineAnime() {
		//given
		String url = TestConstants.NINE_ANIME_TO + "ajax/anime/servers?id=11";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.NINEANIME, TestConstants.NINE_ANIME_TO);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.nineAnime(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForShizaProject() {
		//given
		String url = TestConstants.SHIZA_PROJECT_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.SHIZAPROJECT, TestConstants.SHIZA_PROJECT_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.shizaProject(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForSovetRomantica() {
		//given
		String url = TestConstants.SOVET_ROMANTICA_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.SOVETROMANTICA, TestConstants.SOVET_ROMANTICA_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomantica(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	void shouldBuildHttpRequestServiceDtoForSovetRomanticaWithCookie() {
		//given
		String url = TestConstants.SOVET_ROMANTICA_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		String cookie = "foobar";
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.COOKIE, cookie);
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 521, 522, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		mockFanDubProps(FanDubSource.SOVETROMANTICA, TestConstants.SOVET_ROMANTICA_URL);
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomantica(commonTitle, cookie);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}


	@Test
	void shouldBuildHttpRequestServiceDtoForSovetRomanticaDdosGuard() {
		//given
		String url = TestConstants.SOVET_ROMANTICA_DDOS_GUARD_URL + "check.js";
		HttpMethod method = HttpMethod.GET;
		String referer = TestConstants.SOVET_ROMANTICA_URL;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.REFERER, referer);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		String fallback = StringUtils.EMPTY;
		mockFanDubProps(FanDubSource.SOVETROMANTICA, TestConstants.SOVET_ROMANTICA_URL);
		doReturn(TestConstants.SOVET_ROMANTICA_DDOS_GUARD_URL).when(fanDubProps)
				.getSovetRomanticaDdosGuardUrl();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomanticaDdosGuard();
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	private void mockFanDubProps(FanDubSource fanDubSource, String url) {
		doReturn(Collections.singletonMap(fanDubSource, url)).when(fanDubProps)
				.getUrls();
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

	private CommonTitle getCommonTitle() {
		return CommonTitle.builder()
				.url(TITLE_URL)
				.id("11")
				.malId(42)
				.dataList(2)
				.build();
	}
}