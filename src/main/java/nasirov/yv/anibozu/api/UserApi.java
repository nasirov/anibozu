package nasirov.yv.anibozu.api;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import nasirov.yv.anibozu.dto.user.AnimeList;
import nasirov.yv.anibozu.service.UserServiceI;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Validated
@CrossOrigin(origins = "https://anibozu.nasirov.info")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/{username}")
public class UserApi {

	private final UserServiceI userService;

	@GetMapping("/anime-list")
	public Mono<AnimeList> getAnimeList(@PathVariable("username") @Pattern(regexp = "^[\\w-]{2,16}$") String username) {
		return userService.getAnimeList(username);
	}
}
