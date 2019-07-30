package nasirov.yv;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by nasirov.yv
 */
public class TestUtils {

	public static List<String> getEpisodesRange(String min, String max) {
		List<String> result = new LinkedList<>();
		for (int i = Integer.parseInt(min); i <= Integer.parseInt(max); i++) {
			result.add(String.valueOf(i));
		}
		return result;
	}

}
