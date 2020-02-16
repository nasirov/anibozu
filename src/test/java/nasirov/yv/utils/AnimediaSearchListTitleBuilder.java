package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnimediaSearchListTitleBuilder {

	public static AnimediaSearchListTitle getRegularTitle() {
		return new AnimediaSearchListTitle(REGULAR_TITLE_ID, REGULAR_TITLE_URL, Lists.newArrayList("1", "2", "3", "7"));
	}

	public static AnimediaSearchListTitle getAnnouncement() {
		return new AnimediaSearchListTitle(ANNOUNCEMENT_TITLE_ID, ANNOUNCEMENT_TITLE_URL, null);
	}
}
