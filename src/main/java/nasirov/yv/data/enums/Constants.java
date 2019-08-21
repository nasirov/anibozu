package nasirov.yv.data.enums;

/**
 * Created by nasirov.yv
 */
public enum Constants {
	ZERO_EPISODE("0"),
	FIRST_EPISODE("1"),
	FIRST_DATA_LIST("1"),
	ANNOUNCEMENT_MARK("<a href=\"/announcements\" title=\"Аниме онлайн смотреть\">Анонсы</a>"),
	FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE(""),
	EPISODE_NUMBER_FOR_WATCH_VALUE_IF_EPISODE_IS_NOT_AVAILABLE(""),
	NOT_FOUND_ON_MAL("none");

	private String description;

	Constants(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
}
