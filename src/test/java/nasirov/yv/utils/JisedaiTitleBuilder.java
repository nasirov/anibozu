package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_JESIDAI_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_JISEDAI_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JESIDAI_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.fandub.jisedai.JisedaiTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class JisedaiTitleBuilder {

	public static List<JisedaiTitle> buildJesidaiTitles() {
		return Lists.newArrayList(buildRegularJesidaiTitle(), buildNotFoundOnMalJesidaiTitle());
	}

	public static JisedaiTitle buildRegularJesidaiTitle() {
		return JisedaiTitle.builder()
				.id(REGULAR_TITLE_JESIDAI_ID)
				.url(REGULAR_TITLE_JISEDAI_SITE_URL)
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static JisedaiTitle buildNotFoundOnMalJesidaiTitle() {
		return JisedaiTitle.builder()
				.id(ANNOUNCEMENT_TITLE_JESIDAI_ID)
				.url(ANNOUNCEMENT_TITLE_JISEDAI_SITE_URL)
				.build();
	}
}
