package nasirov.yv.ac.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexViewController {

	@GetMapping("/")
	public Mono<String> getIndexView() {
		return Mono.just("index").doOnSuccess(x -> log.info("Got [{}] view.", x));
	}
}
