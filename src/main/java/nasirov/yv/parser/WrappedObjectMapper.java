package nasirov.yv.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class WrappedObjectMapper {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private WrappedObjectMapper() {
	}

	public static <T, C extends Collection> C unmarshal(String content, Class<T> targetClass, Class<C> collection) {
		CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(collection, targetClass);
		C value = null;
		try {
			value = objectMapper.readValue(content, collectionType);
		} catch (IOException e) {
			log.error("EXCEPTION WHILE UNMARSHALLING", e);
		}
		return value;
	}

	public static <T> T unmarshal(String content, Class<T> targetClass) {
		T value = null;
		try {
			value = objectMapper.readValue(content, targetClass);
		} catch (IOException e) {
			log.error("EXCEPTION WHILE UNMARSHALLING", e);
		}
		return value;
	}

	public static <T extends File, C> void marshal(T objectValue, C content) {
		try {
			objectMapper.writeValue(objectValue, content);
		} catch (IOException e) {
			log.error("EXCEPTION WHILE MARSHALLING TO FILE", e);
		}
	}
}
