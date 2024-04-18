package nasirov.yv.ab.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import nasirov.yv.ab.dto.fe.AnimeListResponse;
import nasirov.yv.ab.service.UserServiceI;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Validated
@CrossOrigin(origins = "https://anibozu.nasirov.info")
@RestController
@RequiredArgsConstructor
public class UserController {

	private static final String USERNAME_VALIDATION_MESSAGE =
			"Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores and dashes only)";

	private final UserServiceI userService;

	@GetMapping("/user/{username}/anime-list")
	public Mono<AnimeListResponse> getAnimeList(
			@PathVariable @NotNull @Pattern(regexp = "^[\\w-]{2,16}$", message = USERNAME_VALIDATION_MESSAGE) String username) {
		return userService.getAnimeList(username);
	}
}