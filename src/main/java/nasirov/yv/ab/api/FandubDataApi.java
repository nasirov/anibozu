package nasirov.yv.ab.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nasirov.yv.ab.mapper.FandubDataMapper;
import nasirov.yv.ab.service.FandubDataServiceI;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anime/{mal-id}/episode/{episode-id}")
public class FandubDataApi {

	private static final ResponseEntity<FandubDataDto> FANDUB_DATA_NOT_FOUND_FALLBACK = ResponseEntity.notFound().build();

	private final FandubDataServiceI fandubDataService;

	private final FandubDataMapper mapper;

	@PutMapping
	public Mono<ResponseEntity<Void>> createOrUpdateFandubData(@PathVariable("mal-id") Integer malId, @PathVariable("episode-id") Integer episodeId,
			@RequestBody @Valid FandubDataDto fandubData) {
		return fandubDataService.createOrUpdateFandubData(mapper.toFandubDataId(malId, episodeId), fandubData).map(this::buildResponse);
	}

	@GetMapping
	public Mono<ResponseEntity<FandubDataDto>> getFandubData(@PathVariable("mal-id") Integer malId, @PathVariable("episode-id") Integer episodeId) {
		return fandubDataService.getFandubData(mapper.toFandubDataId(malId, episodeId)).map(this::buildResponse);
	}

	@DeleteMapping
	public Mono<ResponseEntity<Void>> deleteFandubData(@PathVariable("mal-id") Integer malId, @PathVariable("episode-id") Integer episodeId) {
		return fandubDataService.deleteFandubData(mapper.toFandubDataId(malId, episodeId)).map(this::buildResponse);
	}

	private ResponseEntity<Void> buildResponse(Boolean result) {
		return ResponseEntity.status(result ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	private ResponseEntity<FandubDataDto> buildResponse(FandubDataDto fandubDataDto) {
		return CollectionUtils.isNotEmpty(fandubDataDto.episodes()) ? ResponseEntity.ok(fandubDataDto) : FANDUB_DATA_NOT_FOUND_FALLBACK;
	}
}
