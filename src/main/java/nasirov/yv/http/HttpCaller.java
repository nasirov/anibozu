package nasirov.yv.http;

import java.util.Map;
import nasirov.yv.response.HttpResponse;
import org.springframework.http.HttpMethod;

/**
 * Created by nasirov.yv
 */
public interface HttpCaller {

	HttpResponse call(String url, HttpMethod method, Map<String, Map<String, String>> parameters);
}
