package nasirov.yv.ab.service;

import nasirov.yv.ab.dto.fe.AnimeListResponse;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface UserServiceI {

	Mono<AnimeListResponse> getAnimeList(String username);
}
