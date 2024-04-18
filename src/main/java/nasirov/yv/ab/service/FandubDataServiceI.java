package nasirov.yv.ab.service;

import nasirov.yv.ab.dto.fandub_data.FandubDataId;
import nasirov.yv.starter.common.dto.fandub.common.FandubDataDto;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface FandubDataServiceI {

	Mono<Boolean> createOrUpdateFandubData(FandubDataId fandubDataId, FandubDataDto fandubData);

	Mono<FandubDataDto> getFandubData(FandubDataId fandubDataId);

	Mono<Boolean> deleteFandubData(FandubDataId fandubDataId);
}
