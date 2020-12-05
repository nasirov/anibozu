package nasirov.yv.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.UrlBasedViewResolverRegistration;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;

/**
 * @author Nasirov Yuriy
 */
@EnableWebFlux
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

	private String[] pathPattens;

	private String[] resourceLocations;

	public WebFluxConfig() {
		pathPattens = new String[]{"/css/**", "/fonts/**", "/img/**", "/js/**"};
		resourceLocations = new String[]{"classpath:/static/css/", "classpath:/static/fonts/", "classpath:/static/img/", "classpath:/static/js/"};
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(pathPattens)
				.addResourceLocations(resourceLocations)
				.setCacheControl(CacheControl.noStore());
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		UrlBasedViewResolverRegistration urlBasedViewResolverRegistration = registry.freeMarker();
		urlBasedViewResolverRegistration.suffix(".ftlh");
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() {
		FreeMarkerConfigurer result = new FreeMarkerConfigurer();
		result.setTemplateLoaderPath("classpath:/templates");
		return result;
	}
}
