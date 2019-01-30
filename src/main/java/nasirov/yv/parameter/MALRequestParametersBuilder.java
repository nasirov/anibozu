package nasirov.yv.parameter;

import nasirov.yv.enums.RequestParameters;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.*;

/**
 * Created by Хикка on 21.01.2019.
 */
@Component("malRequestParametersBuilder")
public class MALRequestParametersBuilder implements RequestParametersBuilder {
	@Override
	public Map<String, Map<String, String>> build() {
		Map<String, Map<String, String>> params = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		Map<String, String> accepts = new HashMap<>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
		headers.put("Upgrade-Insecure-Requests", "1");
		accepts.put("", APPLICATION_ATOM_XML + "," + APPLICATION_XHTML_XML + "," + APPLICATION_XML + "," + TEXT_HTML + ";q=0.9,*/*;q=0.8");
		params.put(RequestParameters.HEADER.getDescription(), headers);
		params.put(RequestParameters.ACCEPT.getDescription(), accepts);
		return params;
	}
}
