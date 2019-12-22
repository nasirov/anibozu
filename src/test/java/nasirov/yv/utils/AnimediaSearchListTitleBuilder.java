package nasirov.yv.utils;

import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_URL;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_ID;
import static nasirov.yv.utils.TestConstants.CONCRETIZED_TITLE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_URL;

import java.util.Set;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
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
		return new AnimediaSearchListTitle(REGULAR_TITLE_ID, REGULAR_TITLE_URL);
	}

	public static AnimediaSearchListTitle getAnnouncement() {
		return new AnimediaSearchListTitle(null, ANNOUNCEMENT_TITLE_URL);
	}

	public static AnimediaSearchListTitle getNewTitle() {
		return new AnimediaSearchListTitle(CONCRETIZED_TITLE_ID, CONCRETIZED_TITLE_URL);
	}

	// TODO: 18.12.2019 need to remove after fix json for 16521
	public static AnimediaSearchListTitle tempStub() {
		return new AnimediaSearchListTitle("16521", "anime/sudba-velikij-prikaz-vaviloniya");
	}
}
