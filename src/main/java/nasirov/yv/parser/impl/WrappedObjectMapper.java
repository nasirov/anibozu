package nasirov.yv.parser.impl;

import static org.apache.commons.io.FileUtils.touch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.WrappedObjectMapperI;
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
	public <C> void marshal(File resultFile, C content) {
		touch(resultFile);
		objectMapper.writeValue(resultFile, content);
		log.info("SUCCESSFULLY MARSHALED {}", resultFile.getName());
	}

	@Override
	public <T, C extends Set> Set<T> unmarshalToSet(String content, Class<T> targetClass, Class<C> collection) {
		return readValue(content, targetClass, collection);
	}

	@Override
	public <T, C extends List> List<T> unmarshalToList(String content, Class<T> targetClass, Class<C> collection) {
		return readValue(content, targetClass, collection);
	}

	@SneakyThrows
	private <T, C extends Collection> T readValue(String content, Class<?> targetClass, Class<C> collection) {
		CollectionType collectionType = objectMapper.getTypeFactory()
				.constructCollectionType(collection, targetClass);
		return objectMapper.readValue(content, collectionType);
	}
}
