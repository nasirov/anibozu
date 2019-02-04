package nasirov.yv.parser;

import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class WrappedObjectMapper {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public <T, C extends Collection> C unmarshal(@NotNull String content, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		if (content == null || targetClass == null || collection == null) {
			throw new NullPointerException("Content, target class or collection class is null!");
		}
		CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(collection, targetClass);
		C value = null;
		try {
			value = objectMapper.readValue(content, collectionType);
		} catch (IOException e) {
			log.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public <T> T unmarshal(@NotNull String content, @NotNull Class<T> targetClass) {
		if (content == null || targetClass == null) {
			throw new NullPointerException("Content or targetClass is  null!");
		}
		T value = null;
		try {
			value = objectMapper.readValue(content, targetClass);
		} catch (IOException e) {
			log.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public <T extends File, C> void marshal(@NotNull T objectValue, @NotNull C content) {
		if (content == null || objectValue == null) {
			throw new NullPointerException("Content or objectValue is null!");
		}
		try {
			objectMapper.writeValue(objectValue, content);
		} catch (IOException e) {
			log.error("Exception while marshalling to file", e);
		}
	}
}
