package nasirov.yv.parser;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class WrappedObjectMapper {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private WrappedObjectMapper() {
	
	}
	
	public static <T, C extends Collection> C unmarshal(@NotNull String content, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(collection, targetClass);
		C value = null;
		try {
			value = objectMapper.readValue(content, collectionType);
		} catch (IOException e) {
			log.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public static <T> T unmarshal(@NotNull String content, @NotNull Class<T> targetClass) {
		T value = null;
		try {
			value = objectMapper.readValue(content, targetClass);
		} catch (IOException e) {
			log.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public static <T extends File, C> void marshal(@NotNull T objectValue, @NotNull C content) {
		try {
			objectMapper.writeValue(objectValue, content);
		} catch (IOException e) {
			log.error("Exception while marshalling to file", e);
		}
	}
}
