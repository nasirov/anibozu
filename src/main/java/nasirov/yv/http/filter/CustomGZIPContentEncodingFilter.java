package nasirov.yv.http.filter;

import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
public class CustomGZIPContentEncodingFilter extends GZIPContentEncodingFilter {

	public CustomGZIPContentEncodingFilter() {
		super(true);
	}
}
