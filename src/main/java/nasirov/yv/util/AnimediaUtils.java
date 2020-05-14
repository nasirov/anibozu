package nasirov.yv.util;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_MAL;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.AnimediaTitle;
import org.springframework.util.CollectionUtils;

/**
 * Util operations with Animedia
 * <p>
 * Created by nasirov.yv
 */
@UtilityClass
public class AnimediaUtils {

	private static final Pattern MAX_EPISODES_IS_UNDEFINED = Pattern.compile("^[xXхХ]{1,3}$");

	private static final Pattern JOINED_EPISODES_PATTERN = Pattern.compile("\\d{1,3}-\\d{1,3}");

	public static boolean isMaxEpisodeUndefined(String maxEpisodes) {
		// TODO: 18.12.2019 revert after a json object improvement with max episode in a data list from animedia api
		Matcher matcher = MAX_EPISODES_IS_UNDEFINED.matcher(ofNullable(maxEpisodes).orElse("XXX"));
		return matcher.find();
	}

	public static boolean isAnnouncement(AnimediaSearchListTitle animediaSearchListTitle) {
		return CollectionUtils.isEmpty(animediaSearchListTitle.getDataLists());
	}

	public static boolean isTitleConcretizedAndOngoing(AnimediaTitle animediaTitle) {
		return animediaTitle.getMinOnAnimedia() != null && animediaTitle.getMaxOnAnimedia() != null && animediaTitle.getCurrentMaxOnAnimedia() == null
				&& animediaTitle.getMinOnMAL() != null && animediaTitle.getMaxOnMAL() != null;
	}

	public static boolean isTitleUpdated(AnimediaTitle animediaTitle) {
		// TODO: 29.12.2019 rollback after improvement api object
		return animediaTitle.getMinOnAnimedia() != null
				//&& animediaTitle.getMaxOnAnimedia() != null
				&& animediaTitle.getCurrentMaxOnAnimedia() != null;
	}

	public static boolean isTitleNotFoundOnMAL(AnimediaTitle animediaTitle) {
		return NOT_FOUND_ON_MAL.equals(animediaTitle.getTitleNameOnMAL());
	}

	public static boolean isTitleConcretizedOnMAL(AnimediaTitle animediaTitle) {
		return animediaTitle.getMinOnMAL() != null && animediaTitle.getMaxOnMAL() != null;
	}

	public static String getCorrectCurrentMax(String currentMax) {
		String result;
		String[] joinedEpisodes = splitJoinedEpisodes(currentMax);
		if (joinedEpisodes.length > 1) {
			result = joinedEpisodes[joinedEpisodes.length - 1];
		} else {
			result = currentMax;
		}
		return result;
	}

	public static String getCorrectFirstEpisodeAndMin(String firstEpisodeAndMin) {
		String result;
		String[] joinedEpisodes = splitJoinedEpisodes(firstEpisodeAndMin);
		if (joinedEpisodes.length > 1) {
			result = joinedEpisodes[0];
		} else {
			result = firstEpisodeAndMin;
		}
		return result;
	}

	public static String getFirstEpisode(List<String> episodesList) {
		int firstIndex = 0;
		return episodesList.get(firstIndex);
	}

	public static String getLastEpisode(List<String> episodesList) {
		int lastIndex = episodesList.size() - 1;
		return episodesList.get(lastIndex);
	}

	public static String[] splitJoinedEpisodes(String episode) {
		return episode.split("-");
	}

	public static boolean isJoinedEpisodes(String episode) {
		return JOINED_EPISODES_PATTERN.matcher(episode)
				.find();
	}
}
