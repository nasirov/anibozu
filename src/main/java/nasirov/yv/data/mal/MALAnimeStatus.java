package nasirov.yv.data.mal;

/**
 * Title status in a user anime list on MAL
 * Created by nasirov.yv
 */
public enum MALAnimeStatus {
	/**
	 * Currently Watching
	 */
	WATCHING(1), /**
	 * Completed
	 */
	COMPLETED(2), /**
	 * On Hold
	 */
	ON_HOLD(3), /**
	 * Dropped
	 */
	DROPPED(4), /**
	 * Plan to Watch
	 */
	PLAN_TO_WATCH(5);

	private Integer code;

	public Integer getCode() {
		return code;
	}

	MALAnimeStatus(Integer code) {
		this.code = code;
	}
}
