package nasirov.yv.service;

import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
public interface IndexViewServiceI {

	Mono<String> getIndexView(Model model);
}
