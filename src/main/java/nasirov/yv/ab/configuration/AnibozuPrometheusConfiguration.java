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
		return httpServer -> httpServer.metrics(true, x -> x.replaceAll("/process/.+", "/process/{username}"));
	}
}
