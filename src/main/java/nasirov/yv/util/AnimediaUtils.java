package nasirov.yv.util;

import static nasirov.yv.data.enums.Constants.ANNOUNCEMENT_MARK;
import static nasirov.yv.data.enums.Constants.NOT_FOUND_ON_MAL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;

/**
 * Util operations with Animedia
 * Created by nasirov.yv
 */
public class AnimediaUtils {

	private static final Pattern MAX_EPISODES_IS_UNDEFINED = Pattern.compile("^[xXхХ]{1,3}$");


	private AnimediaUtils() {
	}

	public static boolean isMaxEpisodesUndefined(String maxEpisodes) {
		Matcher matcher = MAX_EPISODES_IS_UNDEFINED.matcher(maxEpisodes);
		return matcher.find();
	}

	public static boolean isAnnouncement(String html) {
		return html.contains(ANNOUNCEMENT_MARK.getDescription());
	}

	public static boolean isTitleConcretizedAndOngoing(AnimediaMALTitleReferences reference) {
		return reference.getMinConcretizedEpisodeOnAnimedia() != null && reference.getMaxConcretizedEpisodeOnAnimedia() != null
				&& reference.getCurrentMax() == null && reference.getFirstEpisode() != null && reference.getMinConcretizedEpisodeOnMAL() != null
				&& reference.getMaxConcretizedEpisodeOnMAL() != null;
	}

	public static boolean isTitleUpdated(AnimediaMALTitleReferences reference) {
		return reference.getMinConcretizedEpisodeOnAnimedia() != null && reference.getMaxConcretizedEpisodeOnAnimedia() != null
				&& reference.getCurrentMax() != null && reference.getFirstEpisode() != null;
	}

	public static boolean isTitleNotFoundOnMAL(AnimediaMALTitleReferences reference) {
		return reference.getTitleOnMAL().equalsIgnoreCase(NOT_FOUND_ON_MAL.getDescription());
	}

	public static boolean isTitleConcretizedOnMAL(AnimediaMALTitleReferences reference) {
		return reference.getMinConcretizedEpisodeOnMAL() != null && reference.getMaxConcretizedEpisodeOnMAL() != null;
	}

}
