package nasirov.yv.http.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import nasirov.yv.http.filter.BrotliContentEncodingFilter;
import nasirov.yv.http.filter.CloudflareDDoSProtectionAvoidingFilter;
import nasirov.yv.http.filter.RepeatRequestFilter;
import nasirov.yv.http.properties.HttpProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Component
public class CustomClient extends Client {

	@Autowired
	public CustomClient(GZIPContentEncodingFilter gzipContentEncodingFilter, BrotliContentEncodingFilter brotliContentEncodingFilter,
			CloudflareDDoSProtectionAvoidingFilter cloudflareDDoSProtectionAvoidingFilter, RepeatRequestFilter repeatRequestFilter, HttpProps httpProps) {
		this.addFilter(gzipContentEncodingFilter);
		this.addFilter(brotliContentEncodingFilter);
		this.addFilter(cloudflareDDoSProtectionAvoidingFilter);
		this.addFilter(repeatRequestFilter);
		this.setConnectTimeout(httpProps.getConnectTimeout());
		this.setReadTimeout(httpProps.getReadTimeout());
		this.setFollowRedirects(httpProps.getFollowRedirects());
	}


}
