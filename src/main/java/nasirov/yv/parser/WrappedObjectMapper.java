package nasirov.yv.parser;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class WrappedObjectMapper {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public <T, C extends Collection> C unmarshal(@NotNull String content, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(collection, targetClass);
		C value = null;
		try {
			value = OBJECT_MAPPER.readValue(content, collectionType);
		} catch (IOException e) {
			log.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public <T> T unmarshal(@NotNull String content, @NotNull Class<T> targetClass) {
		T value = null;
		try {
			value = OBJECT_MAPPER.readValue(content, targetClass);
		} catch (IOException e) {
			log.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public <T extends File, C> void marshal(@NotNull T objectValue, @NotNull C content) {
		try {
			OBJECT_MAPPER.writeValue(objectValue, content);
		} catch (IOException e) {
			log.error("Exception while marshalling to file", e);
		}
	}
}
