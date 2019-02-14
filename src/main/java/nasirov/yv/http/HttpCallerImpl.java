package nasirov.yv.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.research.ws.wadl.HTTPMethods;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.response.HttpResponse;
import org.brotli.dec.BrotliInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static nasirov.yv.enums.RequestParameters.*;

/**
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class HttpCallerImpl implements HttpCaller {
	private static final String BROTLI_HEADER = "br";
	
	private static final String CONTENT_ENCODING = "Content-Encoding";
	
	/**
	 * Sends http request
	 *
	 * @param url        the url for request
	 * @param method     the request method
	 * @param parameters the request parameters
	 * @return the http response with content and status
	 */
	@Override
	public HttpResponse call(@NotNull String url, @NotNull HTTPMethods method, @NotNull Map<String, Map<String, String>> parameters) {
		Client client = new Client();
		client.addFilter(new GZIPContentEncodingFilter(true));
		client.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(5));
		client.setReadTimeout((int) TimeUnit.MINUTES.toMillis(5));
		WebResource webResource = client.resource(url);
		ClientResponse response = sendRequest(enrichRequest(webResource, parameters), method);
		int status = response.getStatus();
		if (status != HttpStatus.OK.value()
				&& status != HttpStatus.NOT_FOUND.value()
				&& status != HttpStatus.FORBIDDEN.value()) {
			for (int i = 0; i < 3; i++) {
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.error("{}\nRepeat request", response.toString());
				response = sendRequest(enrichRequest(webResource, parameters), method);
				if (response.getStatus() == HttpStatus.OK.value()) {
					break;
				}
			}
		}
		boolean isContentEncodingIsBrotli = Stream.of(response.getHeaders())
				.map(Map::entrySet)
				.flatMap(Collection::stream)
				.filter(stringListEntry -> stringListEntry.getKey().equals(CONTENT_ENCODING))
				.flatMap(stringListEntry -> stringListEntry.getValue().stream())
				.anyMatch(list -> list.contains(BROTLI_HEADER));
		String content;
		if (isContentEncodingIsBrotli) {
			content = decodeFromBrotli(response.getEntityInputStream());
		} else {
			content = response.getEntity(String.class);
		}
		return new HttpResponse(content, status);
	}
	
	/**
	 * Decode from brotli
	 *
	 * @param brotliEncodedInputStream the input stream of brotli encoded content
	 * @return utf-8 string
	 */
	private String decodeFromBrotli(InputStream brotliEncodedInputStream) {
		String encodedString = null;
		try {
			BrotliInputStream brotliInputStream = new BrotliInputStream(brotliEncodedInputStream);
			encodedString = StreamUtils.copyToString(brotliInputStream, Charset.forName("UTF-8"));
		} catch (IOException e) {
			log.error("Exception while decoding from brotli", e);
		}
		return encodedString;
	}
	
	/**
	 * Enrich the request with the parameters
	 *
	 * @param webResource the web resource with url
	 * @param parameters  the request parameters
	 */
	private WebResource.Builder enrichRequest(WebResource webResource, Map<String, Map<String, String>> parameters) {
		WebResource.Builder requestBuilder = webResource.getRequestBuilder();
		Stream.of(parameters).filter(map -> map.containsKey(HEADER.getDescription()))
				.flatMap(map -> map.get(HEADER.getDescription()).entrySet().stream())
				.forEach(map -> requestBuilder.header(map.getKey(), map.getValue()));
		Stream.of(parameters).filter(map -> map.containsKey(COOKIE.getDescription()))
				.flatMap(map -> map.get(COOKIE.getDescription()).entrySet().stream())
				.forEach(map -> requestBuilder.cookie(new Cookie(map.getKey(), map.getValue())));
		Stream.of(parameters).filter(map -> map.containsKey(ACCEPT.getDescription()))
				.flatMap(map -> map.get(ACCEPT.getDescription()).entrySet().stream())
				.forEach(map -> requestBuilder.accept(map.getValue()));
		return requestBuilder;
	}
	
	/**
	 * Sends http request depends on http method
	 *
	 * @param requestBuilder the request builder
	 * @param method         the http request method
	 * @return the http response
	 */
	private ClientResponse sendRequest(WebResource.Builder requestBuilder, HTTPMethods method) {
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
