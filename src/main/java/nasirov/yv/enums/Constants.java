package nasirov.yv.enums;

/**
 * Общие константы
 * Created by Хикка on 23.12.2018.
 */
public enum Constants {
	STUB_FOR_UNDEFINED_NUM_OF_EPISODES("0"),
	UNDEFINED_NUM_OF_EPISODES_MAL("0"),
	UNDEFINED_NUM_OF_EPISODES_ANIMEDIA("xxx"),
	ONLINE_ANIMEDIA_TV("http://online.animedia.tv/"),
	ANIMEDIA_ANIME_LIST("http://online.animedia.tv/ajax/anime_list/"),
	ANIMEDIA_ANIME_EPISODES_LIST("http://online.animedia.tv/ajax/episodes/"),
	MAL_R00T_PATH("https://myanimelist.net/"),
	FIRST_EPISODE("1"),
	ANNOUNCEMENT("<a href=\"/announcements\" title=\"Аниме онлайн смотреть\">Анонсы</a>");
	
	private String description;
	
	public String getDescription() {
		return description;
	}
	
	Constants(String description) {
		this.description = description;
	}
}
