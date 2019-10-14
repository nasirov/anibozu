package nasirov.yv.http.filter;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.http.properties.HttpProps;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepeatRequestFilter extends ClientFilter {

	private final HttpProps httpProps;

	@Override
	public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
		ClientHandler clientHandler = getNext();
		ClientResponse clientResponse = getClientResponse(clientHandler, clientRequest);
		int status = getClientResponseStatus(clientResponse);
		URI uri = clientRequest.getURI();
		for (int i = 0; i < httpProps.getMaxAttempts() && isStatusRetryable(status); i++) {
			log.error("TRYING TO SEND REQUEST TO {}", uri);
			makePause();
			clientResponse = getClientResponse(clientHandler, clientRequest);
			status = getClientResponseStatus(clientResponse);
			uri = clientRequest.getURI();
		}
		if (clientResponse == null) {
			log.error("ALL REQUESTS TO {} HAVE BEEN FAILED", uri);
			throw new ClientHandlerException("REQUEST FAILURE");
		}
		return clientResponse;
	}

	private boolean isStatusRetryable(int status) {
		return status == HttpStatus.TOO_MANY_REQUESTS.value() || status == HttpStatus.GATEWAY_TIMEOUT.value() || status == HttpStatus.FOUND.value()
				|| status == HttpStatus.BAD_GATEWAY.value() || status == HttpStatus.INTERNAL_SERVER_ERROR.value();
	}

	@SneakyThrows
	private void makePause() {
		TimeUnit.SECONDS.sleep(httpProps.getMaxWaitTime());
	}

	private ClientResponse getClientResponse(ClientHandler clientHandler, ClientRequest clientRequest) {
		ClientResponse clientResponse = null;
		try {
			clientResponse = clientHandler.handle(clientRequest);
		} catch (ClientHandlerException e) {
			log.error("EXCEPTION DURING SENDING REQUEST TO {} , EXCEPTION MESSAGE {}", clientRequest.getURI(), e.getMessage());
		}
		return clientResponse;
	}

	private int getClientResponseStatus(ClientResponse clientResponse) {
		return clientResponse != null ? clientResponse.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR.value();
	}
}
