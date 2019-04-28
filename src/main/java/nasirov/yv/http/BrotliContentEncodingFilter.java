package nasirov.yv.http;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.brotli.dec.BrotliInputStream;
import org.springframework.http.HttpHeaders;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class BrotliContentEncodingFilter extends ClientFilter {

	private static final String BROTLI_HEADER = "br";

	@Override
	public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
		ClientResponse response = getNext().handle(clientRequest);
		MultivaluedMap<String, String> headers = response.getHeaders();
		if (response.hasEntity() && headers.containsKey(HttpHeaders.CONTENT_ENCODING)) {
			String encoding = headers.getFirst(HttpHeaders.CONTENT_ENCODING);
			if (BROTLI_HEADER.equals(encoding)) {
				headers.remove(HttpHeaders.CONTENT_ENCODING);
				InputStream entityStream = response.getEntityInputStream();
				try {
					response.setEntityInputStream(new BrotliInputStream(entityStream));
				} catch (IOException e) {
					if (entityStream != null) {
						try {
							entityStream.close();
						} catch (IOException e2) {
							log.error("EXCEPTION WHILE CLOSING ENTITY INPUT STREAM!", e2);
						}
					}
					throw new ClientHandlerException(e);
				}
			}
		}
		return response;
	}
}
