package nasirov.yv.parser;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created by nasirov.yv
 */
public interface WrappedObjectMapperI {

	<C> void marshal(File resultFile, C content);

	<T, C extends Set> Set<T> unmarshalToSet(String content, Class<T> targetClass, Class<C> collection);

	<T, C extends List> List<T> unmarshalToList(String content, Class<T> targetClass, Class<C> collection);
}
