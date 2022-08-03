package nasirov.yv.util;

import lombok.experimental.UtilityClass;
import nasirov.yv.starter.common.dto.mal.MalTitle;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class MalUtils {

	public static Integer getNextEpisodeForWatch(MalTitle watchingTitle) {
		return watchingTitle.getNumWatchedEpisodes() + 1;
	}
}
