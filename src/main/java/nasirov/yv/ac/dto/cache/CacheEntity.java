package nasirov.yv.ac.dto.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.starter.common.constant.FandubSource;
import nasirov.yv.starter.common.dto.fandub.common.CommonTitle;

/**
 * @author Nasirov Yuriy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntity {

	@JsonTypeInfo(use = Id.CLASS)
	private FandubSource fandubSource;

	@JsonSerialize(keyUsing = IntegerAsMapKeySerializer.class)
	@JsonDeserialize(keyUsing = IntegerAsMapKeyDeserializer.class)
	private Map<Integer, List<CommonTitle>> malIdToCommonTitles;
}
