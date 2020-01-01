package nasirov.yv.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.springframework.util.ResourceUtils.getFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.Collection;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class IOUtils {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@SneakyThrows
	public static String readFromFile(String pathToFile) {
		return readFileToString(getFile(pathToFile), UTF_8);
	}

	@SneakyThrows
	public static <T, C extends Collection> C unmarshal(String content, Class<T> targetClass, Class<C> collection) {
		CollectionType collectionType = OBJECT_MAPPER.getTypeFactory()
				.constructCollectionType(collection, targetClass);
		return OBJECT_MAPPER.readValue(content, collectionType);
	}

	@SneakyThrows
	public static <T> T unmarshal(String content, Class<T> targetClass) {
		return OBJECT_MAPPER.readValue(content, targetClass);
	}
}
