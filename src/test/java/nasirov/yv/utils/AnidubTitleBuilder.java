package nasirov.yv.utils;

import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_MAL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.anidub.AnidubTitle;
import nasirov.yv.data.anidub.AnidubTitleCategory;
import nasirov.yv.data.anidub.AnidubTitleStatus;
import nasirov.yv.data.anidub.Category;
import nasirov.yv.data.anidub.Status;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnidubTitleBuilder {


	public static Set<AnidubTitle> buildAnidubTitle() {
		return Sets.newHashSet(buildRegularAnidubTitle(), buildNotFoundOnMal());
	}

	public static AnidubTitle buildRegularAnidubTitle() {
		return AnidubTitle.builder()
				.id(REGULAR_TITLE_ANIDUB_ID)
				.category(AnidubTitleCategory.builder()
						.id(Category.SERIES.getId())
						.name(Category.SERIES.getName())
						.build())
				.status(AnidubTitleStatus.builder()
						.id(Status.COMPLETED.getId())
						.name(Status.COMPLETED.getName())
						.build())
				.originalName(REGULAR_TITLE_NAME)
				.ruName("название на русском")
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static AnidubTitle buildNotFoundOnMal() {
		return AnidubTitle.builder()
				.id(ANNOUNCEMENT_TITLE_MAL_ANIME_ID)
				.category(AnidubTitleCategory.builder()
						.id(Category.SERIES.getId())
						.name(Category.SERIES.getName())
						.build())
				.status(AnidubTitleStatus.builder()
						.id(Status.COMPLETED.getId())
						.name(Status.COMPLETED.getName())
						.build())
				.originalName(NOT_FOUND_ON_MAL)
				.ruName("название на русском")
				.titleIdOnMal(null)
				.build();
	}
}
