package nasirov.yv.ab.controller;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.service.ProcessServiceI;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class ProcessController {

	public static final String USERNAME_VALIDATION_MESSAGE = "Please enter a valid mal username between 2 and 16 characters"
			+ "(latin letters, numbers, underscores and dashes only)";

	private final ProcessServiceI processService;

	@GetMapping("/process/{username}")
	public Mono<ProcessResult> process(
			@PathVariable @NotNull @Pattern(regexp = "^[\\w-]{2,16}$", message = USERNAME_VALIDATION_MESSAGE) String username) {
		return processService.process(username);
	}
}
