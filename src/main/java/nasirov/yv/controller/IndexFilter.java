package nasirov.yv.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * @author Nasirov Yuriy
 */
@Component
public class IndexFilter implements Filter {

	private static final String NO_CACHE_NO_STORE_MUST_REVALIDATE = "no-cache, no-store, must-revalidate";

	private static final List<String> INDEX_URLS = Arrays.asList("/", "/index");

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		String requestURI = httpServletRequest.getRequestURI();
		if (INDEX_URLS.contains(requestURI)) {
			httpServletResponse.setHeader(HttpHeaders.CACHE_CONTROL, NO_CACHE_NO_STORE_MUST_REVALIDATE);
		}
		chain.doFilter(request, response);
	}
}
