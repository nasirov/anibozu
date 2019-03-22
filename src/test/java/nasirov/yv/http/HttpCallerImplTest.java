package nasirov.yv.http;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.sun.jersey.api.client.ClientHandlerException;
import nasirov.yv.AbstractTest;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parameter.MALRequestParametersBuilder;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.util.RoutinesIO;
import org.junit.Rule;
import org.junit.Test;
import org.meteogroup.jbrotli.BrotliStreamCompressor;
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {HttpCallerImpl.class,
		AnimediaRequestParametersBuilder.class,
		MALRequestParametersBuilder.class})
public class HttpCallerImplTest extends AbstractTest {
	private static final String BROTLI = "br";
	
	private static final String GZIP = "gzip";
	
	private static final String HOST = "http://localhost:8080";
	
	private static final String URL = "/test";
	
	private static final String TEST_BODY = "testBody";
	
	private static final String CF_COOKIE = "__cfduid";
	
	private static final String CF_CLEARANCE = "cf_clearance";
	
	static {
		BrotliLibraryLoader.loadBrotli();
	}
	
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
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule();
	
	@Test
	public void brotliResponseOK() {
		Map<String, Map<String, String>> requestParams = animediaRequestParametersBuilder.build();
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
		Map<String, Map<String, String>> requestParams = malRequestParametersBuilder.build();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
			gzipOutputStream.write(TEST_BODY.getBytes());
			gzipOutputStream.flush();
		}
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_ENCODING, GZIP)
						.withBody(byteArrayOutputStream.toByteArray())));
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
	}
	
	@Test(expected = ClientHandlerException.class)
	public void notGZIPResponse() throws IOException {
		Map<String, Map<String, String>> requestParams = malRequestParametersBuilder.build();
		stubFor(get(urlPathEqualTo(URL))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_ENCODING, GZIP)
						.withBody(TEST_BODY)));
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
	}
	
	@Test
	public void tooManyRequests() throws IOException {
		String repeatRequestScenario = "repeatRequestScenario";
		String repeatRequestState = "repeatRequestState";
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(repeatRequestScenario)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(HttpStatus.TOO_MANY_REQUESTS.value())
						.withBody(TEST_BODY))
				.willSetStateTo(repeatRequestState));
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(repeatRequestScenario)
				.whenScenarioStateIs(repeatRequestState)
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withBody(TEST_BODY)));
		httpCaller.call(HOST + URL, HttpMethod.GET, malRequestParametersBuilder.build());
		verify(2, getRequestedFor(urlPathEqualTo(URL)));
	}
	
	@Test
	public void avoidDDoSProtection() {
		String ddosProtectionScenario = "ddosProtectionScenario";
		String verificationRequestState = "verificationRequestState";
		String redirectionRequestState = "redirectionRequestState";
		Map<String, Map<String, String>> requestParams = animediaRequestParametersBuilder.build();
		String cfduidCookieValueFromFirstResponse = "__cfduid=d8b040a07f373f9e8d339900293e506821552331240; expires=Tue, 10-Mar-20 19:07:20 GMT; path=/; domain=.animedia.tv; HttpOnly; Secure";
		String cfduidCookieValueFromSecondResponse = "__cfduid=d68132f63eb16421a69619256c999e54d1552331244; expires=Tue, 10-Mar-20 19:07:24 GMT; path=/; domain=.animedia.tv; HttpOnly; Secure";
		String cfClearanceCookieValue = "cf_clearance=aa8303d5738c258f80819ea743ba1fe9628491fd-1552331244-28800-150; path=/; expires=Tue, 12-Mar-19 04:07:24 GMT; domain=.animedia.tv; HttpOnly";
		String verificationUrl = "/cdn-cgi/l/chk_jschl?s=9d0aaf7a0b3157f59adfd2f429a412e6a613f2ec-1552331240-1800-ATF7ze/hpQxe5hACBFqEz1jCWAi7mc81UlMKpn3ZgcT7VUT3WsyZZ+ROkdUSxRY96y9dQyugvHCVlmlxNicA1NtCSVqBJVw090q8o9c15fFB&jschl_vc=e6608832f1dd24e9c032111e373fedae&pass=1552331244.31-l7kKFWKlpL&jschl_answer=21.5393795269";
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(ddosProtectionScenario)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
						.withHeader(HttpHeaders.SET_COOKIE, cfduidCookieValueFromFirstResponse)
						.withBody(RoutinesIO.readFromResource(htmlWithObfuscatedJsCode)))
				.willSetStateTo(verificationRequestState));
		stubFor(get(urlEqualTo(verificationUrl))
				.inScenario(ddosProtectionScenario)
				.whenScenarioStateIs(verificationRequestState)
				.withHeader(HttpHeaders.COOKIE, equalTo(getCookieValue(CF_COOKIE, cfduidCookieValueFromFirstResponse)))
				.willReturn(aResponse()
						.withStatus(HttpStatus.FOUND.value())
						.withHeader(HttpHeaders.SET_COOKIE, cfduidCookieValueFromSecondResponse)
						.withHeader(HttpHeaders.SET_COOKIE, cfClearanceCookieValue)
						.withHeader(HttpHeaders.LOCATION, HOST + URL)
						.withBody("302 Found"))
				.willSetStateTo(redirectionRequestState));
		stubFor(get(urlPathEqualTo(URL))
				.inScenario(ddosProtectionScenario)
				.whenScenarioStateIs(redirectionRequestState)
				.withHeader(HttpHeaders.COOKIE, equalTo(getCookieValue(CF_COOKIE, cfduidCookieValueFromFirstResponse) + "; " + getCookieValue(CF_CLEARANCE, cfClearanceCookieValue)))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withBody(TEST_BODY)));
		HttpResponse response = httpCaller.call(HOST + URL, HttpMethod.GET, requestParams);
		assertEquals(HttpStatus.OK.value(), response.getStatus().intValue());
		assertEquals(TEST_BODY, response.getContent());
		checkHeaders(getAllServeEvents(), getHttpHeadersList(requestParams));
		System.out.println();
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
		Stream.of(params)
				.flatMap(map -> map.entrySet().stream())
				.flatMap(map -> map.getValue().entrySet().stream())
				.forEach(map -> requestHeaders.add(new HttpHeader(map.getKey(), map.getValue())));
		return requestHeaders;
	}
	
	private void checkHeaders(List<ServeEvent> allServeEvents, List<HttpHeader> requestHeaders) {
		Collection<HttpHeader> httpHeaders = allServeEvents.stream().findFirst().orElseThrow(AssertionError::new).getRequest().getHeaders().all();
		requestHeaders.forEach(header -> assertTrue(httpHeaders.contains(header)));
	}
}