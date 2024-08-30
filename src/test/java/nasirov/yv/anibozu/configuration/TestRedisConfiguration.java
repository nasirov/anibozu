package nasirov.yv.anibozu.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.ServerSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import redis.embedded.RedisServer;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
public class TestRedisConfiguration {

	private final RedisServer redisServer;

	public TestRedisConfiguration(RedisProperties redisProperties) {
		int port = getRandomPort();
		redisProperties.setPort(port);
		this.redisServer = RedisServer.builder().port(port).setting("maxmemory 64M").build();
	}

	@PostConstruct
	public void postConstruct() {
		redisServer.start();
		log.info("Started redis server at {}", redisServer.ports());
	}

	@PreDestroy
	public void preDestroy() {
		redisServer.stop();
	}

	private Integer getRandomPort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (Exception e) {
			log.error("Failed to get random port!");
			throw new RuntimeException(e);
		}
	}
}
