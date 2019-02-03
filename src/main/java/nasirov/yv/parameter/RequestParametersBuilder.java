package nasirov.yv.parameter;

import java.util.Map;

/**
 * Interface for http request parameters builder
 * Created by nasirov.yv
 */
public interface RequestParametersBuilder {
	Map<String, Map<String, String>> build();
}
