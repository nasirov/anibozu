package nasirov.yv.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.service.IndexViewServiceI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexViewController {

	private final IndexViewServiceI indexViewService;

	@GetMapping({"/", "/index"})
	public Mono<String> getIndexView(Model model) {
		log.info("Received a request for index view.");
		return indexViewService.getIndexView(model);
	}
}
