package nasirov.yv.http;

import com.sun.research.ws.wadl.HTTPMethods;
import nasirov.yv.response.HttpResponse;

import java.util.Map;

/**
 * Created by nasirov.yv
 */
public interface HttpCaller {
	HttpResponse call(String url, HTTPMethods method, Map<String, Map<String, String>> parameters);
}
