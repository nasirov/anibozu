package nasirov.yv.anibozu.configuration;

import nasirov.yv.anibozu.properties.AppProps;
import nasirov.yv.anibozu.properties.AppProps.Security.Admin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class AnibozuSecurityConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public MapReactiveUserDetailsService userDetailsService(AppProps appProps, PasswordEncoder passwordEncoder) {
		Admin admin = appProps.getSecurity().getAdmin();
		return new MapReactiveUserDetailsService(User.builder()
				.passwordEncoder(passwordEncoder::encode)
				.username(admin.getUsername())
				.password(admin.getPassword())
				.roles(admin.getRoles().toArray(String[]::new))
				.build());
	}

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http.csrf(CsrfSpec::disable)
				.httpBasic(Customizer.withDefaults())
				.authorizeExchange(
						exchanges -> exchanges.pathMatchers("/actuator/**", "/api/v1/user/**").permitAll().pathMatchers("/api/v1/anime/**").hasRole("ADMIN"))
				.build();
	}
}
