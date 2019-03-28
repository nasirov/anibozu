package nasirov.yv.http;

import static nasirov.yv.enums.RequestParameters.ACCEPT;
import static nasirov.yv.enums.RequestParameters.COOKIE;
import static nasirov.yv.enums.RequestParameters.HEADER;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Cookie;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.response.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class HttpCallerImpl implements HttpCaller {

	private Client client;

	public HttpCallerImpl() {
		client = Client.create();
		client.addFilter(new GZIPContentEncodingFilter(true));
		client.addFilter(new BrotliContentEncodingFilter());
		client.addFilter(new CloudflareDDoSProtectionAvoidingFilter());
		client.addFilter(new RepeatRequestFilter());
		client.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(5));
		client.setReadTimeout((int) TimeUnit.MINUTES.toMillis(5));
		client.setFollowRedirects(false);
	}

	/**
	 * Sends http request
	 *
	 * @param url the url for request
	 * @param method the request method
	 * @param parameters the request parameters
	 * @return the http response with content and status
	 */
	@Override
	public HttpResponse call(@NotNull String url, @NotNull HttpMethod method, @NotNull Map<String, Map<String, String>> parameters) {
		WebResource webResource = client.resource(url);
		ClientResponse response = sendRequest(enrichRequest(webResource, parameters), method);
		return new HttpResponse(response.getEntity(String.class), response.getStatus());
	}

	/**
	 * Enrich the request with parameters
	 *
	 * @param webResource the web resource with url
	 * @param parameters the request parameters
	 */
	private WebResource.Builder enrichRequest(WebResource webResource, Map<String, Map<String, String>> parameters) {
		WebResource.Builder requestBuilder = webResource.getRequestBuilder();
		Stream.of(parameters).filter(map -> map.containsKey(HEADER.getDescription())).flatMap(map -> map.get(HEADER.getDescription()).entrySet()
				.stream())
				.forEach(map -> requestBuilder.header(map.getKey(), map.getValue()));
		Stream.of(parameters).filter(map -> map.containsKey(COOKIE.getDescription())).flatMap(map -> map.get(COOKIE.getDescription()).entrySet()
				.stream())
				.forEach(map -> requestBuilder.cookie(new Cookie(map.getKey(), map.getValue())));
		Stream.of(parameters).filter(map -> map.containsKey(ACCEPT.getDescription())).flatMap(map -> map.get(ACCEPT.getDescription()).entrySet()
				.stream())
				.forEach(map -> requestBuilder.accept(map.getValue()));
		return requestBuilder;
	}

	/**
	 * Sends http request depends on http method
	 *
	 * @param requestBuilder the request builder
	 * @param method the http request method
	 * @return the http response
	 */
	private ClientResponse sendRequest(WebResource.Builder requestBuilder, HttpMethod method) {
		ClientResponse response;
		switch (method) {
			case GET:
				response = requestBuilder.get(ClientResponse.class);
				break;
			case POST:
				response = requestBuilder.post(ClientResponse.class);
				break;
			default:
				throw new UnsupportedOperationException("Method " + method + " is not supported");
		}
		return response;
	}
}
