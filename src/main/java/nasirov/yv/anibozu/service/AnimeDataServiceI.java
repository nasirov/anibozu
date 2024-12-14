package nasirov.yv.anibozu.service;

import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import reactor.core.publisher.Mono;

public interface AnimeDataServiceI {

	Mono<Boolean> saveAnimeData(AnimeDataId animeDataId, AnimeData animeData);

	Mono<AnimeData> getAnimeData(AnimeDataId animeDataId);

	Mono<Boolean> deleteAnimeData(AnimeDataId animeDataId);
}
