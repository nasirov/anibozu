package nasirov.yv.http;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class RepeatRequestFilter extends ClientFilter {

	@Override
	public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
		ClientHandler clientHandler = getNext();
		ClientResponse clientResponse = clientHandler.handle(clientRequest);
		int status = clientResponse.getStatus();
		if (status == HttpStatus.TOO_MANY_REQUESTS.value() || status == HttpStatus.GATEWAY_TIMEOUT.value() || status == HttpStatus.FOUND.value()) {
			for (int i = 0; i < 3; i++) {
				try {
					TimeUnit.SECONDS.sleep(4);
				} catch (InterruptedException e) {
					log.error("THREAD WAS INTERRUPTED", e);
					Thread.currentThread().interrupt();
				}
				log.error("{}\nREPEAT REQUEST", clientResponse.toString());
				clientResponse = clientHandler.handle(clientRequest);
				status = clientResponse.getStatus();
				if (status != HttpStatus.TOO_MANY_REQUESTS.value() && status != HttpStatus.GATEWAY_TIMEOUT.value() && status != HttpStatus.FOUND.value()) {
					break;
				}
			}
		}
		return clientResponse;
	}
}
