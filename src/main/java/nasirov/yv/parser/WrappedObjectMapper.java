package nasirov.yv.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WrappedObjectMapper implements WrappedObjectMapperI {

	private final ObjectMapper objectMapper;

	@Override
	@SneakyThrows
	public <T, C extends Collection> C unmarshal(String content, Class<T> targetClass, Class<C> collection) {
		CollectionType collectionType = objectMapper.getTypeFactory()
				.constructCollectionType(collection, targetClass);
		return objectMapper.readValue(content, collectionType);
	}

	@Override
	@SneakyThrows
	public <T> T unmarshal(String content, Class<T> targetClass) {
		return objectMapper.readValue(content, targetClass);
	}

	@Override
	@SneakyThrows
	public <T extends File, C> void marshal(T objectValue, C content) {
		objectMapper.writeValue(objectValue, content);
	}
}
