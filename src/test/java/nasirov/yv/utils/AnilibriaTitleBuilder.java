package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ANILIBRIA_SITE_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_NAME_RU;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANILIBRIA_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME_RU;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.fandub.anilibria.AnilibriaTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnilibriaTitleBuilder {

	private static final String FULL_NAME_DELIMITER = " / ";

	public static List<AnilibriaTitle> buildAnilibriaTitles() {
		return Lists.newArrayList(buildRegularAnilibriaTitle(), buildNotFoundOnMalAnilibriaTitle());
	}

	public static AnilibriaTitle buildRegularAnilibriaTitle() {
		return AnilibriaTitle.builder()
				.url(REGULAR_TITLE_ANILIBRIA_SITE_URL)
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.fullName(REGULAR_TITLE_NAME_RU + FULL_NAME_DELIMITER + REGULAR_TITLE_NAME)
				.ruName(REGULAR_TITLE_NAME_RU)
				.enName(REGULAR_TITLE_NAME)
				.build();
	}

	public static AnilibriaTitle buildNotFoundOnMalAnilibriaTitle() {
		return AnilibriaTitle.builder()
				.url(ANNOUNCEMENT_TITLE_ANILIBRIA_SITE_URL)
				.fullName(ANNOUNCEMENT_TITLE_NAME_RU + FULL_NAME_DELIMITER + ANNOUNCEMENT_TITLE_NAME)
				.ruName(ANNOUNCEMENT_TITLE_NAME_RU)
				.enName(ANNOUNCEMENT_TITLE_NAME)
				.build();
	}
}
