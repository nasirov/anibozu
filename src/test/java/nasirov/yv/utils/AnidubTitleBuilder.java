package nasirov.yv.utils;

import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_MAL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_ANIDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.ANNOUNCEMENT_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_ANIDUB_SITE_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_MAL_ANIME_ID;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_NAME;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.fandub.anidub.api.AnidubApiTitle;
import nasirov.yv.data.fandub.anidub.api.AnidubTitleCategory;
import nasirov.yv.data.fandub.anidub.api.AnidubTitleStatus;
import nasirov.yv.data.fandub.anidub.api.Category;
import nasirov.yv.data.fandub.anidub.api.Status;
import nasirov.yv.data.fandub.anidub.site.AnidubSiteTitle;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class AnidubTitleBuilder {


	public static List<AnidubApiTitle> buildAnidubApiTitles() {
		return Lists.newArrayList(buildRegularAnidubApiTitle(), buildNotFoundOnMalAnidubApiTitle());
	}

	public static List<AnidubSiteTitle> buildAnidubSiteTitles() {
		return Lists.newArrayList(buildRegularAnidubSiteTitle(), buildNotFoundOnMalAnidubSiteTitle());
	}

	public static AnidubSiteTitle buildRegularAnidubSiteTitle() {
		return AnidubSiteTitle.builder()
				.url(REGULAR_TITLE_ANIDUB_SITE_URL)
				.titleIdOnMal(REGULAR_TITLE_MAL_ANIME_ID)
				.build();
	}

	public static AnidubSiteTitle buildNotFoundOnMalAnidubSiteTitle() {
		return AnidubSiteTitle.builder()
				.url(ANNOUNCEMENT_TITLE_ANIDUB_SITE_URL)
				.build();
	}

	public static AnidubApiTitle buildRegularAnidubApiTitle() {
		return AnidubApiTitle.builder()
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

	public static AnidubApiTitle buildNotFoundOnMalAnidubApiTitle() {
		return AnidubApiTitle.builder()
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
