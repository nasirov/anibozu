package nasirov.yv.http.caller;

import static nasirov.yv.data.request.RequestParameters.HEADER;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.response.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpCallerImpl implements HttpCaller {

	private final Client client;

	/**
	 * Sends http request
	 *
	 * @param url        the url for request
	 * @param method     the request method
	 * @param parameters the request parameters
	 * @return the http response with content and status
	 */
	@Override
	public HttpResponse call(String url, HttpMethod method, Map<String, Map<String, String>> parameters) {
		WebResource webResource = client.resource(url);
		ClientResponse response = sendRequest(enrichRequest(webResource, parameters), method);
		return new HttpResponse(response.getEntity(String.class), response.getStatus());
	}

	/**
	 * Enrich the request with parameters
	 *
	 * @param webResource the web resource with url
	 * @param parameters  the request parameters
	 */
	private WebResource.Builder enrichRequest(WebResource webResource, Map<String, Map<String, String>> parameters) {
		WebResource.Builder requestBuilder = webResource.getRequestBuilder();
		Stream.of(parameters).filter(map -> map.containsKey(HEADER.getDescription())).flatMap(map -> map.get(HEADER.getDescription()).entrySet().stream())
				.forEach(map -> requestBuilder.header(map.getKey(), map.getValue()));
		return requestBuilder;
	}

	/**
	 * Sends http request depends on http method
	 *
	 * @param requestBuilder the request builder
	 * @param method         the http request method
	 * @return the http response
	 */
	private ClientResponse sendRequest(WebResource.Builder requestBuilder, HttpMethod method) {
		ClientResponse response;
		if (method == HttpMethod.GET) {
			response = requestBuilder.get(ClientResponse.class);
		} else {
			throw new UnsupportedOperationException("Method " + method + " is not supported");
		}
		return response;
	}
}
