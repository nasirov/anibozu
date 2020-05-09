package nasirov.yv.util;

import lombok.experimental.UtilityClass;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class MalUtils {

	public static Integer getNextEpisodeForWatch(UserMALTitleInfo watchingTitle) {
		return watchingTitle.getNumWatchedEpisodes() + 1;
	}
}
