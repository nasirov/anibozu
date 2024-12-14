package nasirov.yv.anibozu.mapper;

import nasirov.yv.anibozu.dto.anime_data.AnimeDataId;
import nasirov.yv.anibozu.dto.user.EpisodeInfo;
import nasirov.yv.anibozu.model.AnimeDataKey;
import nasirov.yv.anibozu.model.AnimeDataValue;
import nasirov.yv.anibozu.model.AnimeEpisodeData;
import nasirov.yv.starter_common.dto.anibozu.AnimeData;
import nasirov.yv.starter_common.dto.anibozu.EpisodeData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnimeDataMapper {

	AnimeDataId toAnimeDataId(Integer malId, Integer episodeId);

	AnimeDataKey toAnimeDataKey(AnimeDataId dto);

	AnimeDataValue toAnimeDataValue(AnimeData dto);

	AnimeData toAnimeData(AnimeDataValue model);

	EpisodeInfo toEpisodeInfo(EpisodeData dto);

	AnimeEpisodeData toAnimeEpisodeData(EpisodeData dto);

	EpisodeData toEpisodeData(AnimeEpisodeData model);
}
