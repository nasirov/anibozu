package nasirov.yv.util;

import lombok.experimental.UtilityClass;
import nasirov.yv.fandub.service.spring.boot.starter.dto.mal.MalTitle;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class MalUtils {

	public static Integer getNextEpisodeForWatch(MalTitle watchingTitle) {
		return watchingTitle.getNumWatchedEpisodes() + 1;
	}
}
