package nasirov.yv.ab.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import nasirov.yv.ab.model.FandubDataKey;
import nasirov.yv.ab.model.FandubDataValue;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author Nasirov Yuriy
 */
@Configuration
public class AnibozuRedisConfiguration {

	@Bean
	public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(RedisProperties redisProperties) {
		return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
	}

	@Bean
	public ReactiveRedisTemplate<FandubDataKey, FandubDataValue> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
			ObjectMapper mapper) {
		RedisSerializationContext<FandubDataKey, FandubDataValue> serializationContext =
				RedisSerializationContext.<FandubDataKey, FandubDataValue>newSerializationContext(
						RedisSerializer.string())
				.key(new Jackson2JsonRedisSerializer<>(mapper, FandubDataKey.class))
				.value(new Jackson2JsonRedisSerializer<>(mapper, FandubDataValue.class))
				.build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
	}
}
