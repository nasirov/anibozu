package nasirov.yv.parser;

import java.io.File;

/**
 * Created by nasirov.yv
 */
public interface WrappedObjectMapperI {

	<C> void marshal(File resultFile, C content);
}
