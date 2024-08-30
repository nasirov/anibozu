package nasirov.yv.anibozu.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nasirov.yv.anibozu.mapper.AnimeDataMapper;
import nasirov.yv.anibozu.service.AnimeDataServiceI;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
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
public class AnimeDataApi {

	private static final ResponseEntity<AnimeData> ANIME_DATA_NOT_FOUND_FALLBACK = ResponseEntity.notFound().build();

	private final AnimeDataServiceI animeDataService;

	private final AnimeDataMapper mapper;

	@PutMapping
	public Mono<ResponseEntity<Void>> saveAnimeData(@PathVariable("mal-id") Integer malId, @PathVariable("episode-id") Integer episodeId,
			@RequestBody @Valid AnimeData animeData) {
		return animeDataService.saveAnimeData(mapper.toAnimeDataId(malId, episodeId), animeData).map(this::buildResponse);
	}

	@GetMapping
	public Mono<ResponseEntity<AnimeData>> getAnimeData(@PathVariable("mal-id") Integer malId, @PathVariable("episode-id") Integer episodeId) {
		return animeDataService.getAnimeData(mapper.toAnimeDataId(malId, episodeId)).map(this::buildResponse);
	}

	@DeleteMapping
	public Mono<ResponseEntity<Void>> deleteAnimeData(@PathVariable("mal-id") Integer malId, @PathVariable("episode-id") Integer episodeId) {
		return animeDataService.deleteAnimeData(mapper.toAnimeDataId(malId, episodeId)).map(this::buildResponse);
	}

	private ResponseEntity<Void> buildResponse(Boolean result) {
		return ResponseEntity.status(result ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	private ResponseEntity<AnimeData> buildResponse(AnimeData animeData) {
		return CollectionUtils.isNotEmpty(animeData.episodes()) ? ResponseEntity.ok(animeData) : ANIME_DATA_NOT_FOUND_FALLBACK;
	}
}
