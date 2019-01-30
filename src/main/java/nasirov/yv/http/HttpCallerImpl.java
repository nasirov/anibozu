package nasirov.yv.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.research.ws.wadl.HTTPMethods;
import nasirov.yv.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Cookie;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static nasirov.yv.enums.RequestParameters.*;

/**
 * Created by Хикка on 21.01.2019.
 */
@Component
public class HttpCallerImpl implements HttpCaller {
	private static final Logger logger = LoggerFactory.getLogger(HttpCaller.class);
	
	@Override
	public HttpResponse call(@NotNull String url, @NotNull HTTPMethods method, @NotNull Map<String, Map<String, String>> parameters) {
		logger.debug("Start Calling {}", url);
		Client client = new Client();
		client.addFilter(new GZIPContentEncodingFilter(true));
		client.setConnectTimeout(60000);
		client.setReadTimeout(60000);
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
				logger.error("{}\nRepeat request", response.toString());
				response = sendRequest(enrichRequest(webResource, parameters), method);
				if (response.getStatus() == HttpStatus.OK.value()) {
					break;
				}
			}
		}
		String entity = response.getEntity(String.class);
		logger.debug("End Calling {}", url);
		return new HttpResponse(entity, status);
	}
	
	/**
	 * Обогащает запрос параметрами
	 *
	 * @param webResource
	 * @param parameters
	 */
	private WebResource.Builder enrichRequest(WebResource webResource, Map<String, Map<String, String>> parameters) {
		logger.debug("Start Enrich request to {}", webResource.getURI());
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
		logger.debug("End Enrich request to {}", webResource.getURI());
		return requestBuilder;
	}
	
	/**
	 * Отправляет запрос в зависимости от метода
	 *
	 * @param requestBuilder билдер запроса
	 * @param method         http метод
	 * @return ответ от сайта
	 */
	private ClientResponse sendRequest(WebResource.Builder requestBuilder, HTTPMethods method) {
		logger.debug("Start Sending request");
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
		logger.debug("End Sending request");
		return response;
	}
}
