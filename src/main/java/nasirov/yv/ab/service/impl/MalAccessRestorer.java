package nasirov.yv.ab.service.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.service.MalAccessRestorerI;
import nasirov.yv.starter.common.properties.StarterCommonProperties;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import nasirov.yv.starter.reactive.services.service.HttpRequestServiceI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MalAccessRestorer implements MalAccessRestorerI {

	private static final Semaphore SEMAPHORE = new Semaphore(1);

	private final HttpRequestServiceI httpRequestService;

	private final StarterCommonProperties starterCommonProperties;

	@Override
	public Mono<Boolean> restoreMalAccess() {
		return httpRequestService.performHttpRequest(restoreMalAccessDto())
				.mapNotNull(ResponseEntity::getBody)
				.doOnSubscribe(x -> log.info("Trying to restore MAL access..."))
				.doOnSuccess(x -> log.info("Is MAL access restored? [{}]", x ? "YES" : "NO"));
	}

	@Override
	public void restoreMalAccessAsync() {
		if (SEMAPHORE.tryAcquire()) {
			CompletableFuture.runAsync(() -> {
				restoreMalAccess().block();
				SEMAPHORE.release();
			}).exceptionally(e -> {
				SEMAPHORE.release();
				log.error("Exception has occurred during mal access restoring", e);
				return null;
			});
		}
	}

	private HttpRequestServiceDto<ResponseEntity<Boolean>> restoreMalAccessDto() {
		return HttpRequestServiceDto.<ResponseEntity<Boolean>>builder()
				.url(starterCommonProperties.getExternalServices().getMalAccessServiceUrl() + "access/restore")
				.clientResponseFunction(x -> x.toEntity(Boolean.class))
				.fallback(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false))
				.build();
	}
}
