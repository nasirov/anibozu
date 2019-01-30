package nasirov.yv.parser;

import com.sun.istack.NotNull;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Хикка on 30.12.2018.
 */
@Component
public class WrappedObjectMapper {
	private static final Logger logger = LoggerFactory.getLogger(WrappedObjectMapper.class);
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public <T, C extends Collection> C unmarshal(@NotNull String content, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		if (content == null || targetClass == null || collection == null) {
			logger.error("Content, target class, collection class cannot be null!");
			throw new RuntimeException("Content, target class, collection class cannot be null!");
		}
		CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(collection, targetClass);
		C value = null;
		try {
			value = objectMapper.readValue(content, collectionType);
		} catch (IOException e) {
			logger.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public <T> T unmarshal(@NotNull String content, @NotNull Class<T> targetClass) {
		if (content == null || targetClass == null) {
			logger.error("Content or targetClass cannot be null!");
			throw new RuntimeException("Content or targetClass cannot be null!");
		}
		T value = null;
		try {
			value = objectMapper.readValue(content, targetClass);
		} catch (IOException e) {
			logger.error("Exception while unmarshalling", e);
		}
		return value;
	}
	
	public <T extends File, C> void marshal(@NotNull T objectValue, @NotNull C content) {
		if (content == null || objectValue == null) {
			logger.error("Content or objectValue cannot be null!");
			throw new RuntimeException("Content or objectValue cannot be null!");
		}
		try {
			objectMapper.writeValue(objectValue, content);
		} catch (IOException e) {
			logger.error("Exception while marshalling to file", e);
		}
	}
}
