package nasirov.yv.utils;

import lombok.experimental.UtilityClass;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class MalTitleTestFactory {

	public static final Integer REGULAR_TITLE_MAL_ID = 3;

	public static final Integer CONCRETIZED_TITLE_MAL_ID = 5;

	public static final Integer NOT_FOUND_ON_FANDUB_TITLE_ID = 6;

	public static MalTitle buildRegularMalTitle() {
		return buildWatchingTitle("images/anime/" + REGULAR_TITLE_MAL_ID + "/regularTitle" + ".jpg",
				"/anime/" + REGULAR_TITLE_MAL_ID + "/regular%20title%20name", REGULAR_TITLE_MAL_ID, 0);
	}

	public static MalTitle buildConcretizedMalTitle() {
		return buildWatchingTitle("images/anime/" + CONCRETIZED_TITLE_MAL_ID + "/concretizedTitle.jpg",
				"/anime/" + CONCRETIZED_TITLE_MAL_ID + "/concretizedTitle", CONCRETIZED_TITLE_MAL_ID, 10);
	}

	public static MalTitle buildNotFoundOnFandubMalTitle() {
		return buildWatchingTitle("images/anime/" + NOT_FOUND_ON_FANDUB_TITLE_ID + "/notFoundOnMalTitle.jpg",
				"/anime/" + NOT_FOUND_ON_FANDUB_TITLE_ID + "/notFoundOnMalTitle", NOT_FOUND_ON_FANDUB_TITLE_ID, 0);
	}

	public static MalTitle buildWatchingTitle(String posterUrl, String animeUrl, int id, int numWatchedEpisodes) {
		return MalTitle.builder()
				.id(id)
				.name("foo bar baz")
				.numWatchedEpisodes(numWatchedEpisodes)
				.posterUrl("https://cdn.myanimelist.net/" + posterUrl)
				.animeUrl("https://myanimelist.net" + animeUrl)
				.build();
	}
}
