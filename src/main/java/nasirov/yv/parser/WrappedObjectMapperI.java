package nasirov.yv.parser;

import java.io.File;
import java.util.Collection;

/**
 * Created by nasirov.yv
 */
public interface WrappedObjectMapperI {

	<C> void marshal(File resultFile, C content);

	<T, C extends Collection> C unmarshal(String content, Class<T> targetClass, Class<C> collection);
}
