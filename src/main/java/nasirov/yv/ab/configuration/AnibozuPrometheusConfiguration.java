package nasirov.yv.ab.configuration;

import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nasirov Yuriy
 */
@Configuration
public class AnibozuPrometheusConfiguration {

	@Bean
	public NettyServerCustomizer nettyServerCustomizer() {
		return httpServer -> httpServer.metrics(true, x -> x.replaceAll("/api/v1/user/.+/anime-list", "/api/v1/user/{username}/anime-list")
				.replaceAll("/api/v1/anime/\\d+/episode/\\d+", "/api/v1/anime/{mal-id}/episode/{episode-id}"));
	}
}
