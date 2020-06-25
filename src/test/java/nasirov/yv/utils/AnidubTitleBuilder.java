package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ANIDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.fandub.anidub.AnidubTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnidubTitleBuilder {

	public static List<AnidubTitle> buildAnidubTitles() {
		return Lists.newArrayList(buildRegularAnidubTitle(), buildNotFoundOnMalAnidubTitle());
	}

	public static AnidubTitle buildRegularAnidubTitle() {
		return AnidubTitle.builder()
				.url(REGULAR_TITLE_ANIDUB_SITE_URL)
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static AnidubTitle buildNotFoundOnMalAnidubTitle() {
		return AnidubTitle.builder()
				.url(ANNOUNCEMENT_TITLE_ANIDUB_SITE_URL)
				.build();
	}
}
