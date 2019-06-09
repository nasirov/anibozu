package nasirov.yv.http.caller;

import java.util.Map;
import nasirov.yv.data.response.HttpResponse;
import org.springframework.http.HttpMethod;

/**
 * Created by nasirov.yv
 */
public interface HttpCaller {

	HttpResponse call(String url, HttpMethod method, Map<String, Map<String, String>> parameters);
}
