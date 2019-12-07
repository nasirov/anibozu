package nasirov.yv.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicy;
import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryPolicy;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerContext;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;

/**
 * Created by nasirov.yv
 */
@Configuration
@RequiredArgsConstructor
public class RetryConfig {

	private final SpringClientFactory springClientFactory;

	@Bean
	public LoadBalancedRetryFactory retryFactory() {
		return new LoadBalancedRetryFactory() {
			@Override
			public LoadBalancedRetryPolicy createRetryPolicy(String service, ServiceInstanceChooser serviceInstanceChooser) {
				RibbonLoadBalancerContext lbContext = springClientFactory.getLoadBalancerContext(service);
				return new RibbonLoadBalancedRetryPolicy(service, lbContext, serviceInstanceChooser, springClientFactory.getClientConfig(service));
			}
			@Override
			public BackOffPolicy createBackOffPolicy(String service) {
				return new ExponentialBackOffPolicy();
			}
		};
	}
}
