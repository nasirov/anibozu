package nasirov.yv.enums;

/**
 * Статусы аниме в профиле пользователя на MAL
 * Created by Хикка on 19.12.2018.
 */
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
	
	private Integer code;
	
	public Integer getCode() {
		return code;
	}
	
	MALAnimeStatus(Integer code) {
		this.code = code;
	}
}
