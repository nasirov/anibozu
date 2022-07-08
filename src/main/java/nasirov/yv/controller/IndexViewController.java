package nasirov.yv.controller;

import lombok.RequiredArgsConstructor;
import nasirov.yv.service.IndexViewServiceI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Controller
@RequiredArgsConstructor
public class IndexViewController {

	private final IndexViewServiceI indexViewService;

	@GetMapping({"/", "/index"})
	public Mono<String> getIndexView(Model model) {
		return indexViewService.getIndexView(model);
	}
}
