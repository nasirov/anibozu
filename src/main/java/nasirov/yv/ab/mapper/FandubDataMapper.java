package nasirov.yv.ab.mapper;

import nasirov.yv.ab.dto.fandub_data.FandubDataId;
import nasirov.yv.ab.dto.user.EpisodeInfo;
import nasirov.yv.ab.model.FandubDataKey;
import nasirov.yv.ab.model.FandubDataValue;
import nasirov.yv.ab.model.FandubEpisodeData;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import nasirov.yv.starter.common.dto.fandub.common.FandubEpisodeDataDto;
import org.mapstruct.Mapper;

/**
 * @author Nasirov Yuriy
 */
@Mapper(componentModel = "spring")
public interface FandubDataMapper {

	FandubDataId toFandubDataId(Integer malId, Integer episodeId);

	FandubDataKey toFandubDataKey(FandubDataId dto);

	FandubDataValue toFandubDataValue(FandubDataDto dto);

	FandubDataDto toFandubDataDto(FandubDataValue model);

	EpisodeInfo toEpisodeInfo(FandubEpisodeDataDto dto);

	FandubEpisodeData toFandubEpisodeData(FandubEpisodeDataDto dto);

	FandubEpisodeDataDto toFandubEpisodeDataDto(FandubEpisodeData model);
}
