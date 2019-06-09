package nasirov.yv.data.mal;

import lombok.Getter;

/**
 * Created by nasirov.yv
 */
@Getter
public enum MALCategories {
	ANIME("anime");

	private String description;

	MALCategories(String description) {
		this.description = description;
	}
}
