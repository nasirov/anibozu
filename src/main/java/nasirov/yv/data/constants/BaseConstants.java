package nasirov.yv.data.constants;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class BaseConstants {

	public static final String NOT_AVAILABLE_EPISODE_URL = "";

	public static final String NOT_AVAILABLE_EPISODE_NAME = "";

	public static final String TITLE_NOT_FOUND_EPISODE_URL = "not found on a fandub site";

	public static final String TITLE_NOT_FOUND_EPISODE_NAME = "not found on a fandub site";

	public static final Pair<String, String> NOT_AVAILABLE_EPISODE_NAME_AND_URL = Pair.of(NOT_AVAILABLE_EPISODE_NAME, NOT_AVAILABLE_EPISODE_URL);

	public static final Pair<String, String> TITLE_NOT_FOUND_EPISODE_NAME_AND_URL = Pair.of(TITLE_NOT_FOUND_EPISODE_NAME, TITLE_NOT_FOUND_EPISODE_URL);
}
