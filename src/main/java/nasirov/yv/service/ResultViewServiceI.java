package nasirov.yv.service;

import nasirov.yv.data.front.UserInputDto;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface ResultViewServiceI {

	Mono<String> getResultView(UserInputDto userInputDto, Model model);
}
