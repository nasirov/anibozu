package nasirov.yv.ab.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.springframework.util.ResourceUtils.getFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * @author Nasirov Yuriy
 */
@UtilityClass
public class IOUtils {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.registerModules(new ParameterNamesModule());

	@SneakyThrows
	public static String readFromFile(String pathToFile) {
		return readFileToString(getFile(pathToFile), UTF_8);
	}

	@SneakyThrows
	public <T> List<T> unmarshalToListFromString(String content, Class<T> targetClass) {
		CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, targetClass);
		return OBJECT_MAPPER.readValue(content, collectionType);
	}

	@SneakyThrows
	public <T> List<T> unmarshalToListFromFile(String pathToFile, Class<T> targetClass) {
		return unmarshalToListFromString(readFromFile(pathToFile), targetClass);
	}
}
