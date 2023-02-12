package nasirov.yv.ab.service.impl;

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

	private final HttpRequestServiceI httpRequestService;

	private final StarterCommonProperties starterCommonProperties;

	@Override
	public Mono<Boolean> restoreMalAccess() {
		return httpRequestService.performHttpRequest(restoreMalAccessDto())
				.mapNotNull(ResponseEntity::getBody)
				.doOnSubscribe(x -> log.info("Trying to restore MAL access..."))
				.doOnSuccess(x -> log.info("Is MAL access restored? [{}]", x ? "YES" : "NO"));
	}

	private HttpRequestServiceDto<ResponseEntity<Boolean>> restoreMalAccessDto() {
		return HttpRequestServiceDto.<ResponseEntity<Boolean>>builder()
				.url(starterCommonProperties.getExternalServices().getMalAccessServiceUrl() + "access/restore")
				.clientResponseFunction(x -> x.toEntity(Boolean.class))
				.fallback(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false))
				.build();
	}
}
