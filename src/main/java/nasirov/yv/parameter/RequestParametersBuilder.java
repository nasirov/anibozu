package nasirov.yv.parameter;

import java.util.Map;

/**
 * Created by Хикка on 21.01.2019.
 */
public interface RequestParametersBuilder {
	Map<String, Map<String, String>> build();
}
