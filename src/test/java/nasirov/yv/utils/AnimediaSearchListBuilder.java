package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.MULTI_SEASONS_TITLE_URL;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_ANIME_URL;
import static nasirov.yv.utils.TestConstants.SINGLE_SEASON_TITLE_NAME;

import java.util.Set;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;
import org.assertj.core.util.Sets;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnimediaSearchListBuilder {

	public static Set<AnimediaTitleSearchInfo> getAnimediaSearchList() {
		return Sets.newLinkedHashSet(getMultiSeasonsAnime(), getSingleSeasonAnime(), getAnnouncement());
	}

	public static AnimediaTitleSearchInfo getMultiSeasonsAnime() {
		return new AnimediaTitleSearchInfo("мастера меча онлайн",
				"",
				MULTI_SEASONS_TITLE_URL,
				"http://static.animedia.tv/uploads/%D0%9C%D0%90%D0%A1%D0%A2%D0%95%D0%A0%D0%90.jpg?h=350&q=100");
	}

	public static AnimediaTitleSearchInfo getSingleSeasonAnime() {
		return new AnimediaTitleSearchInfo("чёрный клевер",
				SINGLE_SEASON_TITLE_NAME,
				SINGLE_SEASON_ANIME_URL,
				"http://static.animedia.tv/uploads/KLEVER.jpg?h=350&q=100");
	}

	public static AnimediaTitleSearchInfo getAnnouncement() {
		return new AnimediaTitleSearchInfo("ингресс",
				ANNOUNCEMENT_TITLE_NAME,
				ANNOUNCEMENT_TITLE_URL,
				"http://static.animedia.tv/uploads/450.jpg?h=350&q=100");
	}

}
