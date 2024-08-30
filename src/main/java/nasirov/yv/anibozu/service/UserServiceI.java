package nasirov.yv.anibozu.service;

import nasirov.yv.anibozu.dto.user.AnimeList;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface UserServiceI {

	Mono<AnimeList> getAnimeList(String username);
}
