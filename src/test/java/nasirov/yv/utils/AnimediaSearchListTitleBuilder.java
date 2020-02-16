package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ID;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;

import com.google.common.collect.Lists;
import java.util.Set;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.api.Season;
import org.assertj.core.util.Sets;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnimediaSearchListTitleBuilder {

	public static Set<AnimediaSearchListTitle> getAnimediaSearchList() {
		return Sets.newLinkedHashSet(getRegularTitle(), getAnnouncement());
	}

	public static AnimediaSearchListTitle getRegularTitle() {
		return new AnimediaSearchListTitle(REGULAR_TITLE_ID,
				REGULAR_TITLE_URL,
				Lists.newArrayList(new Season("1"), new Season("2"), new Season("3"), new Season("7")));
	}

	public static AnimediaSearchListTitle getAnnouncement() {
		return new AnimediaSearchListTitle(ANNOUNCEMENT_TITLE_ID, ANNOUNCEMENT_TITLE_URL, null);
	}

	public static AnimediaSearchListTitle getNewTitle() {
		return new AnimediaSearchListTitle(CONCRETIZED_TITLE_ID, CONCRETIZED_TITLE_URL, Lists.newArrayList(new Season("7")));
	}
}
