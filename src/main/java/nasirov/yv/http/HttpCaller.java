package nasirov.yv.http;

import nasirov.yv.response.HttpResponse;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Created by nasirov.yv
 */
public interface HttpCaller {
	HttpResponse call(String url, HttpMethod method, Map<String, Map<String, String>> parameters);
}
