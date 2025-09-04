package nasirov.yv.anibozu.mapper;

import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.dto.user.AnimeList.Episode;
import nasirov.yv.anibozu.model.AnimeDataKey;
import nasirov.yv.anibozu.model.AnimeDataValue;
import nasirov.yv.anibozu.model.AnimeEpisodeData;
import nasirov.yv.anibozu.model.AnimeEpisodesData;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import nasirov.yv.starter_common.dto.anibozu.EpisodesData;
import nasirov.yv.starter_common.dto.anibozu.EpisodesData.EpisodeData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnimeDataMapper {

	AnimeDataId toAnimeDataId(Integer malId, Integer episodeId);

	AnimeDataKey toAnimeDataKey(AnimeDataId dto);

	AnimeDataValue toAnimeDataValue(AnimeData dto);

	AnimeData toAnimeData(AnimeDataValue model);

	AnimeEpisodesData toAnimeEpisodesData(EpisodesData dto);

	EpisodesData toEpisodesData(AnimeEpisodesData model);

	AnimeEpisodeData toAnimeEpisodeData(EpisodeData dto);

	EpisodeData toEpisodeData(AnimeEpisodeData model);

	Episode toEpisode(EpisodeData dto);
}
