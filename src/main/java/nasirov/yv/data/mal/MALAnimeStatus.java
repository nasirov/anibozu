package nasirov.yv.data.mal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Title status in a user anime list on MAL
 * <p>
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public enum MALAnimeStatus {
	/**
	 * Currently Watching
	 */
	WATCHING(1),
	/**
	 * Completed
	 */
	COMPLETED(2),
	/**
	 * On Hold
	 */
	ON_HOLD(3),
	/**
	 * Dropped
	 */
	DROPPED(4),
	/**
	 * Plan to Watch
	 */
	PLAN_TO_WATCH(5);

	@Getter
	private final Integer code;
}
