package nasirov.yv.parser;

import java.io.File;
import java.util.Collection;

/**
 * Created by nasirov.yv
 */
public interface WrappedObjectMapperI {

	<T, C extends Collection> C unmarshal(String content, Class<T> targetClass, Class<C> collection);

	<T> T unmarshal(String content, Class<T> targetClass);

	<T extends File, C> void marshal(T objectValue, C content);
}
