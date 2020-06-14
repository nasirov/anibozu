package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ANIMEPIK_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ANIMEPIK_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIMEPIK_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.anime_pik.api.AnimepikTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnimepikTitleBuilder {

	public static List<AnimepikTitle> buildAnimepikTitles() {
		return Lists.newArrayList(buildRegularAnimepikTitle(), buildNotFoundOnMalAnimepikTitle());
	}

	public static AnimepikTitle buildRegularAnimepikTitle() {
		return AnimepikTitle.builder()
				.id(REGULAR_TITLE_ANIMEPIK_ID)
				.url(REGULAR_TITLE_ANIMEPIK_SITE_URL)
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static AnimepikTitle buildNotFoundOnMalAnimepikTitle() {
		return AnimepikTitle.builder()
				.id(ANNOUNCEMENT_TITLE_ANIMEPIK_ID)
				.url(ANNOUNCEMENT_TITLE_ANIMEPIK_SITE_URL)
				.build();
	}
}
