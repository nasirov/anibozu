package nasirov.yv.ab.service;

import java.util.List;
import nasirov.yv.starter.common.dto.mal.MalAnime;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface MalServiceI {

	Mono<List<MalAnime>> getAnimeList(String username);
}
