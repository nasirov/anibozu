package nasirov.yv.parameter;

import static javax.ws.rs.core.MediaType.APPLICATION_XHTML_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.util.HashMap;
import java.util.Map;
import nasirov.yv.enums.RequestParameters;
import org.springframework.http.HttpHeaders;

/**
 * Interface for http request parameters builder
 * Created by nasirov.yv
 */
public interface RequestParametersBuilder {

	default Map<String, Map<String, String>> build() {
		Map<String, Map<String, String>> params = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		Map<String, String> accepts = new HashMap<>();
		headers.put(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0");
		headers.put(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
		headers.put(HttpHeaders.ACCEPT_LANGUAGE, "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("DNT", "1");
		accepts.put(HttpHeaders.ACCEPT, TEXT_HTML + "," + APPLICATION_XHTML_XML + "," + APPLICATION_XML + ";q=0.9,image/webp,*/*;q=0.8");
		params.put(RequestParameters.HEADER.getDescription(), headers);
		params.put(RequestParameters.ACCEPT.getDescription(), accepts);
		return params;
	}
}
