package nasirov.yv.anibozu.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import nasirov.yv.anibozu.model.AnimeDataKey;
import nasirov.yv.anibozu.model.AnimeDataValue;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class AnibozuRedisConfiguration {

	@Bean
	public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(RedisProperties redisProperties) {
		return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
	}

	@Bean
	public ReactiveRedisTemplate<AnimeDataKey, AnimeDataValue> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
			ObjectMapper mapper) {
		RedisSerializationContext<AnimeDataKey, AnimeDataValue> serializationContext =
				RedisSerializationContext.<AnimeDataKey, AnimeDataValue>newSerializationContext(
						RedisSerializer.string())
				.key(new Jackson2JsonRedisSerializer<>(mapper, AnimeDataKey.class))
				.value(new Jackson2JsonRedisSerializer<>(mapper, AnimeDataValue.class))
				.build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
	}
}
