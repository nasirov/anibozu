package nasirov.yv.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.sun.jersey.api.client.ClientHandlerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.response.HttpResponse;
import nasirov.yv.exception.cloudflare.CookieNotFoundException;
import nasirov.yv.exception.cloudflare.SeedNotFoundException;
import nasirov.yv.http.caller.HttpCaller;
import nasirov.yv.http.parameter.RequestParametersBuilder;
import nasirov.yv.util.RoutinesIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.meteogroup.jbrotli.BrotliStreamCompressor;
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Created by nasirov.yv
 */

public class HttpCallerImplTest extends AbstractTest {

	private static final String BROTLI = "br";

	private static final String GZIP = "gzip";

	private static final Integer PORT = 8888;

	private static final String HOST = "http://localhost:" + PORT;

	private static final String URL = "/test";

	private static final String TEST_BODY = "testBody";

	private static final String CF_COOKIE = "__cfduid";

	private static final String CF_CLEARANCE = "cf_clearance";

	private static final String DDOS_PROTECTION = "DDoS protection by Cloudflare";

	private static final String DDOS_PROTECTION_SCENARIO = "ddosProtectionScenario";

	private static final String VERIFICATION_REQUEST_STATE = "verificationRequestState";

	private static final String REDIRECTION_REQUEST_STATE = "redirectionRequestState";

	private static final String REPEAT_REQUEST_SCENARIO = "repeatRequestScenario";

	private static final String REPEAT_REQUEST_STATE = "repeatRequestState";

	private static final String CFDUID_COOKIE_VALUE_FROM_FIRST_RESPONSE =
			"__cfduid=d8b040a07f373f9e8d339900293e506821552331240; expires=Tue, 10-Mar-20 19:07:20 GMT; path=/; domain=.animedia.tv; HttpOnly; " +
					"Secure";

	private static final String CFDUID_COOKIE_VALUE_FROM_SECOND_RESPONSE =
			"__cfduid=d68132f63eb16421a69619256c999e54d1552331244; expires=Tue, 10-Mar-20 19:07:24 GMT; path=/; domain=.animedia.tv; HttpOnly; " +
					"Secure";

	private static final String CF_CLEARANCE_COOKIE_VALUE =
			"cf_clearance=aa8303d5738c258f80819ea743ba1fe9628491fd-1552331244-28800-150; path=/; expires=Tue, 12-Mar-19 "
					+ "04:07:24 GMT; domain=.animedia.tv; HttpOnly";

	private static final String VERIFICATION_URL = "/cdn-cgi/l/chk_jschl?s=9d0aaf7a0b3157f59adfd2f429a412e6a613f2ec-1552331240-1800-ATF7ze"
			+ "/hpQxe5hACBFqEz1jCWAi7mc81UlMKpn3ZgcT7VUT3WsyZZ+ROkdUSxRY96y9dQyugvHCVlmlxNicA1NtCSVqBJVw090q8o9c15fFB&jschl_vc"
			+ "=e6608832f1dd24e9c032111e373fedae&pass=1552331244.31-l7kKFWKlpL&jschl_answer=21.5393795269";

	static {
		BrotliLibraryLoader.loadBrotli();
	}

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8888);

	@Value("classpath:animedia/htmlWithObfuscatedJsCode.txt")
	private Resource htmlWithObfuscatedJsCode;

	@Autowired
	private HttpCaller httpCaller;

	@Autowired
	@Qualifier("animediaRequestParametersBuilder")
	private RequestParametersBuilder animediaRequestParametersBuilder;

	@Autowired
	@Qualifier("animediaRequestParametersBuilder")
	private RequestParametersBuilder malRequestParametersBuilder;

	private Map<String, Map<String, String>> requestParams;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		requestParams = animediaRequestParametersBuilder.build();
	}

	@After
	public void tearDown() {
		wireMockRule.resetAll();
	}

	@Test
	public void brotliResponseOK() {
		byte[] testBodyBytes = TEST_BODY.getBytes(StandardCharsets.UTF_8);
		BrotliStreamCompressor brotliStreamCompressor = new BrotliStreamCompressor();
		byte[] brotliCompressedArray = brotliStreamCompressor.compressArray(testBodyBytes, true);
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_ENCODING, BROTLI)
						.withBody(brotliCompressedArray)));
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
	}

	@Test(expected = ClientHandlerException.class)
	public void notBrotliResponse() {
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_ENCODING, BROTLI)
						.withBody(TEST_BODY)));
		httpCaller.call(HOST + URL, HttpMethod.GET, animediaRequestParametersBuilder.build());
	}

	@Test
	public void gzipResponseOK() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
			gzipOutputStream.write(TEST_BODY.getBytes());
			gzipOutputStream.flush();
		}
		stubGzip(byteArrayOutputStream.toByteArray());
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
	}

	@Test(expected = ClientHandlerException.class)
	public void notGZIPResponse() {
		stubGzip(TEST_BODY.getBytes());
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
	}

	@Test
	public void tooManyRequests() {
		stubRepeatRequest(HttpStatus.TOO_MANY_REQUESTS);
		httpCaller.call(HOST + URL, HttpMethod.GET, malRequestParametersBuilder.build());
		verify(2, getRequestedFor(urlPathEqualTo(URL)));
	}

	@Test
	public void gatewayTimeout() {
		stubRepeatRequest(HttpStatus.GATEWAY_TIMEOUT);
		httpCaller.call(HOST + URL, HttpMethod.GET, malRequestParametersBuilder.build());
		verify(2, getRequestedFor(urlPathEqualTo(URL)));
	}

	@Test
	public void statusFound() {
		stubRepeatRequest(HttpStatus.FOUND);
		httpCaller.call(HOST + URL, HttpMethod.GET, malRequestParametersBuilder.build());
		verify(2, getRequestedFor(urlPathEqualTo(URL)));
	}

	@Test
	public void badGateway() {
		stubRepeatRequest(HttpStatus.BAD_GATEWAY);
		httpCaller.call(HOST + URL, HttpMethod.GET, malRequestParametersBuilder.build());
		verify(2, getRequestedFor(urlPathEqualTo(URL)));
	}

	@Test
	public void internalServerError() {
		stubRepeatRequest(HttpStatus.INTERNAL_SERVER_ERROR);
		httpCaller.call(HOST + URL, HttpMethod.GET, malRequestParametersBuilder.build());
		verify(2, getRequestedFor(urlPathEqualTo(URL)));
	}

	@Test
	public void avoidDDoSProtection() {
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(DDOS_PROTECTION_SCENARIO)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
						.withHeader(HttpHeaders.SET_COOKIE, CFDUID_COOKIE_VALUE_FROM_FIRST_RESPONSE)
						.withBody(RoutinesIO.readFromResource(htmlWithObfuscatedJsCode)))
				.willSetStateTo(VERIFICATION_REQUEST_STATE));
		stubFor(get(urlEqualTo(VERIFICATION_URL))
				.inScenario(DDOS_PROTECTION_SCENARIO)
				.whenScenarioStateIs(VERIFICATION_REQUEST_STATE)
				.withHeader(HttpHeaders.COOKIE, equalTo(getCookieValue(CF_COOKIE, CFDUID_COOKIE_VALUE_FROM_FIRST_RESPONSE)))
				.willReturn(aResponse().withStatus(HttpStatus.FOUND.value())
						.withHeader(HttpHeaders.SET_COOKIE, CFDUID_COOKIE_VALUE_FROM_SECOND_RESPONSE)
						.withHeader(HttpHeaders.SET_COOKIE, CF_CLEARANCE_COOKIE_VALUE)
						.withHeader(HttpHeaders.LOCATION, HOST + URL).withBody("302 Found"))
				.willSetStateTo(REDIRECTION_REQUEST_STATE));
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(DDOS_PROTECTION_SCENARIO)
				.whenScenarioStateIs(REDIRECTION_REQUEST_STATE)
				.withHeader(HttpHeaders.COOKIE,
						equalTo(
								getCookieValue(CF_COOKIE, CFDUID_COOKIE_VALUE_FROM_FIRST_RESPONSE) + "; " +
										getCookieValue(CF_CLEARANCE, CF_CLEARANCE_COOKIE_VALUE)))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withBody(TEST_BODY)));
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
	}

	@Test(expected = SeedNotFoundException.class)
	public void avoidDDoSProtectionSeedNotFound() {
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
						.withBody(DDOS_PROTECTION)));
		httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
	}

	@Test(expected = CookieNotFoundException.class)
	public void avoidDDoSProtectionRequiredCfduidCookieNotAvailable() {
		String cfduidCookieValueFromFirstResponse = "";
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
				.withHeader(HttpHeaders.SET_COOKIE, cfduidCookieValueFromFirstResponse)
						.withBody(RoutinesIO.readFromResource(htmlWithObfuscatedJsCode))));
		httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
	}

	@Test(expected = CookieNotFoundException.class)
	public void avoidDDoSProtectionRequiredCfClearanceCookieNotAvailable() {
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(DDOS_PROTECTION_SCENARIO)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
						.withHeader(HttpHeaders.SET_COOKIE, CFDUID_COOKIE_VALUE_FROM_FIRST_RESPONSE)
						.withBody(RoutinesIO.readFromResource(htmlWithObfuscatedJsCode)))
				.willSetStateTo(VERIFICATION_REQUEST_STATE));
		stubFor(get(urlEqualTo(VERIFICATION_URL))
				.inScenario(DDOS_PROTECTION_SCENARIO)
				.whenScenarioStateIs(VERIFICATION_REQUEST_STATE)
				.withHeader(HttpHeaders.COOKIE, equalTo(getCookieValue(CF_COOKIE, CFDUID_COOKIE_VALUE_FROM_FIRST_RESPONSE)))
				.willReturn(aResponse()
						.withStatus(HttpStatus.FOUND.value())
						.withHeader(HttpHeaders.SET_COOKIE, CFDUID_COOKIE_VALUE_FROM_SECOND_RESPONSE)
						.withHeader(HttpHeaders.SET_COOKIE, "")
						.withHeader(HttpHeaders.LOCATION, HOST + URL)
						.withBody("302 Found"))
				.willSetStateTo(REDIRECTION_REQUEST_STATE));
		httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
	}

	@Test
	public void avoidDDoSProtectionServiceUnavailableNotDDoSProtection() {
		stubFor(get(urlPathEqualTo(URL)).willReturn(aResponse().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value()).withBody(TEST_BODY)));
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void notSupportedHttpMethod() {
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withBody(TEST_BODY)));
		httpCaller.call(HOST + URL, HttpMethod.POST, requestParams);
	}

	private void stubGzip(byte[] body) {
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_ENCODING, GZIP)
						.withBody(body)));
	}

	private void stubRepeatRequest(HttpStatus responseStatus) {
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(REPEAT_REQUEST_SCENARIO)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
						.withBody(TEST_BODY))
				.willSetStateTo(REPEAT_REQUEST_STATE));
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(REPEAT_REQUEST_SCENARIO)
				.whenScenarioStateIs(REPEAT_REQUEST_STATE)
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withBody(TEST_BODY)));
	}

	private String getCookieValue(String cookie, String value) {
		String result = null;
		Pattern pattern = Pattern.compile("(?<value>__cfduid=.*?);");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			return matcher.group("value");
		}
		switch (cookie) {
			case CF_COOKIE:
				pattern = Pattern.compile("(?<value>__cfduid=.*?);");
				matcher = pattern.matcher(value);
				if (matcher.find()) {
					result = matcher.group("value");
				}
				break;
			case CF_CLEARANCE:
				pattern = Pattern.compile("(?<value>cf_clearance=.*?);");
				matcher = pattern.matcher(value);
				if (matcher.find()) {
					result = matcher.group("value");
				}
				break;
			default:
				break;
		}
		return result;
	}

	private List<HttpHeader> getHttpHeadersList(Map<String, Map<String, String>> params) {
		List<HttpHeader> requestHeaders = new ArrayList<>();
		Stream.of(params).flatMap(map -> map.entrySet().stream()).flatMap(map -> map.getValue().entrySet().stream())
				.forEach(map -> requestHeaders.add(new HttpHeader(map.getKey(), map.getValue())));
		return requestHeaders;
	}

	private void checkHeaders(List<ServeEvent> allServeEvents, List<HttpHeader> requestHeaders) {
		Collection<HttpHeader> httpHeaders = allServeEvents.stream().findFirst().orElseThrow(AssertionError::new).getRequest().getHeaders().all();
		requestHeaders.forEach(header -> assertTrue(httpHeaders.contains(header)));
	}
}