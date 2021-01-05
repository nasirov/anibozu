package nasirov.yv.service.impl.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animedia.AnimediaEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.animepik.AnimepikEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal_service.MalServiceResponseDto;
import nasirov.yv.fandub.service.spring.boot.starter.dto.selenium_service.SeleniumServiceRequestDto;
import nasirov.yv.fandub.service.spring.boot.starter.properties.ExternalServicesProps;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.utils.TestConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpRequestServiceDtoBuilderTest {

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

	@Before
	public void setUp() {
		mockFanDubProps();
		mockExternalServicesProps();
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForMalService() {
		//given
		String url = MAL_SERVICE_URL + "titles?username=foobar&status=WATCHING";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, MAL_SERVICE_BASIC_AUTH);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		MalServiceResponseDto fallback = MalServiceResponseDto.builder()
				.username("foobar")
				.malTitles(Collections.emptyList())
				.errorMessage(StringUtils.EMPTY)
				.build();
		//when
		HttpRequestServiceDto<MalServiceResponseDto> result = httpRequestServiceDtoBuilder.malService("foobar", MalTitleWatchingStatus.WATCHING);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForFandubTitlesService() {
		//given
		String url = FANDUB_TITLES_SERVICE_URL + "titles?fanDubSource=ANIMEDIA&malId=42&malEpisodeId=1";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, FANDUB_TITLES_SERVICE_BASIC_AUTH);
		Set<Integer> retryableStatusCodes = Collections.emptySet();
		List<CommonTitle> fallback = Collections.emptyList();
		//when
		HttpRequestServiceDto<List<CommonTitle>> result = httpRequestServiceDtoBuilder.fandubTitlesService(FanDubSource.ANIMEDIA, 42, 1);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForSeleniumService() {
		//given
		String url = SELENIUM_SERVICE_URL + "content?url=https://foo.bar&timeoutInSec=5&cssSelector=a";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.singletonMap(HttpHeaders.AUTHORIZATION, SELENIUM_SERVICE_BASIC_AUTH);
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
	public void shouldBuildHttpRequestServiceDtoForAnidub() {
		//given
		String url = TestConstants.ANIDUB_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.anidub(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForAnilibria() {
		//given
		String url = TestConstants.ANILIBRIA_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.anilibria(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForAnimedia() {
		//given
		String url = TestConstants.ANIMEDIA_ONLINE_TV + "embeds/playlist-j.txt/11/2";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		List<AnimediaEpisode> fallback = Collections.emptyList();
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<List<AnimediaEpisode>> result = httpRequestServiceDtoBuilder.animedia(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForAnimepik() {
		//given
		String url = TestConstants.ANIMEPIK_RESOURCES_URL + "11.txt";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		List<AnimepikEpisode> fallback = Collections.emptyList();
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<List<AnimepikEpisode>> result = httpRequestServiceDtoBuilder.animepik(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForJisedai() {
		//given
		String url = TestConstants.JISEDAI_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.jisedai(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForJutsu() {
		//given
		String url = TestConstants.JUTSU_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.jutsu(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForNineAnime() {
		//given
		String url = TestConstants.NINE_ANIME_TO + "ajax/anime/servers?id=foo42";
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.nineAnime(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForShizaProject() {
		//given
		String url = TestConstants.SHIZA_PROJECT_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.shizaProject(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	@Test
	public void shouldBuildHttpRequestServiceDtoForSovetRomantica() {
		//given
		String url = TestConstants.SOVET_ROMANTICA_URL + TITLE_URL;
		HttpMethod method = HttpMethod.GET;
		Map<String, String> headers = Collections.emptyMap();
		Set<Integer> retryableStatusCodes = Sets.newHashSet(500, 502, 503, 504, 520, 524);
		String fallback = StringUtils.EMPTY;
		CommonTitle commonTitle = getCommonTitle();
		//when
		HttpRequestServiceDto<String> result = httpRequestServiceDtoBuilder.sovetRomantica(commonTitle);
		//then
		checkResult(result, url, method, headers, retryableStatusCodes, fallback);
	}

	private void mockFanDubProps() {
		EnumMap<FanDubSource, String> urls = new EnumMap<>(FanDubSource.class);
		urls.put(FanDubSource.ANIDUB, TestConstants.ANIDUB_URL);
		urls.put(FanDubSource.ANILIBRIA, TestConstants.ANILIBRIA_URL);
		urls.put(FanDubSource.ANIMEDIA, TestConstants.ANIMEDIA_ONLINE_TV);
		urls.put(FanDubSource.ANIMEPIK, TestConstants.ANIMEPIK_URL);
		urls.put(FanDubSource.JISEDAI, TestConstants.JISEDAI_URL);
		urls.put(FanDubSource.JUTSU, TestConstants.JUTSU_URL);
		urls.put(FanDubSource.NINEANIME, TestConstants.NINE_ANIME_TO);
		urls.put(FanDubSource.SHIZAPROJECT, TestConstants.SHIZA_PROJECT_URL);
		urls.put(FanDubSource.SOVETROMANTICA, TestConstants.SOVET_ROMANTICA_URL);
		doReturn(urls).when(fanDubProps)
				.getUrls();
		doReturn(TestConstants.ANIMEPIK_RESOURCES_URL).when(fanDubProps)
				.getAnimepikResourcesUrl();
	}

	private void mockExternalServicesProps() {
		doReturn(FANDUB_TITLES_SERVICE_BASIC_AUTH).when(externalServicesProps)
				.getFandubTitlesServiceBasicAuth();
		doReturn(FANDUB_TITLES_SERVICE_URL).when(externalServicesProps)
				.getFandubTitlesServiceUrl();
		doReturn(MAL_SERVICE_BASIC_AUTH).when(externalServicesProps)
				.getMalServiceBasicAuth();
		doReturn(MAL_SERVICE_URL).when(externalServicesProps)
				.getMalServiceUrl();
		doReturn(SELENIUM_SERVICE_BASIC_AUTH).when(externalServicesProps)
				.getSeleniumServiceBasicAuth();
		doReturn(SELENIUM_SERVICE_URL).when(externalServicesProps)
				.getSeleniumServiceUrl();
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
				.id(11)
				.malId(42)
				.dataId("foo42")
				.dataList(2)
				.build();
	}
}