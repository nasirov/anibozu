package nasirov.yv.ab.service;

import nasirov.yv.ab.dto.user.AnimeList;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface UserServiceI {

	Mono<AnimeList> getAnimeList(String username);
}
