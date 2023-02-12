package nasirov.yv.ab.service;

import java.util.List;
import nasirov.yv.starter.common.dto.mal.MalTitle;
import nasirov.yv.starter.common.dto.mal.MalTitleWatchingStatus;
import nasirov.yv.starter.reactive.services.dto.HttpRequestServiceDto;
import org.springframework.http.ResponseEntity;

/**
 * @author Nasirov Yuriy
 */
public interface HttpRequestServiceDtoBuilderI {

	HttpRequestServiceDto<ResponseEntity<String>> buildUserProfileDto(String username);

	HttpRequestServiceDto<ResponseEntity<List<MalTitle>>> buildPartOfTitlesDto(Integer currentOffset, String username,
			MalTitleWatchingStatus status);
}
