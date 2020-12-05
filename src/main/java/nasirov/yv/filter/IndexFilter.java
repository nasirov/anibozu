package nasirov.yv.filter;

import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Component
public class IndexFilter implements WebFilter {

	private static final String NO_STORE_MUST_REVALIDATE = "no-store, must-revalidate";

	private static final List<String> INDEX_URLS = Arrays.asList("/", "/index");

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String requestPath = exchange.getRequest()
				.getURI()
				.getPath();
		if (INDEX_URLS.contains(requestPath)) {
			exchange.getResponse()
					.getHeaders()
					.add(HttpHeaders.CACHE_CONTROL, NO_STORE_MUST_REVALIDATE);
		}
		return chain.filter(exchange);
	}
}
