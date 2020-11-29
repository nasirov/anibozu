package nasirov.yv.configuration;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

/**
 * @author Nasirov Yuriy
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

	private String[] pathPattens;

	private String[] resourceLocations;

	private VersionResourceResolver versionResourceResolver;

	public WebMvcConfig() {
		pathPattens = new String[]{"/css/**", "/fonts/**", "/img/**", "/js/**"};
		resourceLocations = new String[]{"classpath:/static/css/", "classpath:/static/fonts/", "classpath:/static/img/", "classpath:/static/js/"};
		versionResourceResolver = new VersionResourceResolver().addVersionStrategy(new ContentVersionStrategy(), "/**");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(pathPattens).addResourceLocations(resourceLocations).setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
				.resourceChain(true).addResolver(versionResourceResolver);
	}
}
