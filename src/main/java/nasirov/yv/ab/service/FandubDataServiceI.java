package nasirov.yv.ab.service;

import nasirov.yv.ab.dto.internal.FandubData;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface FandubDataServiceI {

	Mono<FandubData> getFandubData();
}
