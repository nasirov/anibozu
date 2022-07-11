package nasirov.yv.controller;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.InputDto;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FandubSource;
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

	@GetMapping("/")
	public Mono<String> getIndexView(Model model) {
		model.addAttribute("inputDto", new InputDto());
		Arrays.stream(FandubSource.values()).forEach(x -> model.addAttribute(x.getName(), x));
		return Mono.just("index").doOnSuccess(x -> log.info("Got [{}] view.", x));
	}
}
