package nasirov.yv.parser;

import static nasirov.yv.utils.TestConstants.TEMP_FOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.parser.impl.WrappedObjectMapper;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

/**
 * Created by nasirov.yv
 */
public class WrappedObjectMapperTest {

	private final WrappedObjectMapperI wrappedObjectMapperI = new WrappedObjectMapper(new ObjectMapper());

	private final RoutinesIO routinesIO = new RoutinesIO(wrappedObjectMapperI);

	@Test
	public void unmarshalCollectionOk() {
		List<Anime> unmarshal = wrappedObjectMapperI.unmarshal(getCollectionJson(), Anime.class, ArrayList.class);
		assertNotNull(unmarshal);
		assertFalse(unmarshal.isEmpty());
		assertEquals(2, unmarshal.size());
		Anime element = unmarshal.get(0);
		assertEquals("1", element.getId());
		assertEquals("https://online.animedia.tv/anime/pyat-nevest/1/1", element.getFullUrl());
		assertEquals("anime/pyat-nevest", element.getRootUrl());
		element = unmarshal.get(1);
		assertEquals("2", element.getId());
		assertEquals("https://online.animedia.tv/anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove/1/1", element.getFullUrl());
		assertEquals("anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove", element.getRootUrl());
	}
	@Test(expected = MismatchedInputException.class)
	public void unmarshalCollectionNullValue() {
		wrappedObjectMapperI.unmarshal("", String.class, List.class);
	}

	@Test(expected = MismatchedInputException.class)
	public void unmarshalNullValue() {
		wrappedObjectMapperI.unmarshal("", String.class);
	}

	@Test
	public void unmarshalSingleElementOk() {
		Anime singleElement = wrappedObjectMapperI.unmarshal(getSingleElementJson(), Anime.class);
		assertNotNull(singleElement);
		assertEquals("1", singleElement.getId());
		assertEquals("https://online.animedia.tv/anime/pyat-nevest/1/1", singleElement.getFullUrl());
		assertEquals("anime/pyat-nevest", singleElement.getRootUrl());
	}

	@Test
	public void marshalToFileOk() {
		File tempFile = new File("test.txt");
		assertFalse(tempFile.exists());
		Anime forMarshal = new Anime("1", "https://online.animedia.tv/anime/pyat-nevest/1/1", "anime/pyat-nevest");
		wrappedObjectMapperI.marshal(tempFile, forMarshal);
		assertTrue(tempFile.exists());
		String fromFile = routinesIO.readFromFile(tempFile);
		FileSystemUtils.deleteRecursively(tempFile);
		assertEquals(getSingleElementJson(), fromFile);
		assertFalse(tempFile.exists());
	}

	@Test(expected = FileNotFoundException.class)
	public void marshalException() {
		routinesIO.removeDir("temp");
		File tempDir = new File(TEMP_FOLDER_NAME + File.separator + "test.txt");
		assertFalse(tempDir.exists());
		Anime forMarshal = new Anime("1", "https://online.animedia.tv/anime/pyat-nevest/1/1", "anime/pyat-nevest");
		wrappedObjectMapperI.marshal(tempDir, forMarshal);
		assertFalse(tempDir.exists());
	}

	private String getCollectionJson() {
		return "[\n" + "  {\n" + "    \"id\": \"1\",\n" + "    \"fullUrl\": \"https://online.animedia.tv/anime/pyat-nevest/1/1\",\n"
				+ "    \"rootUrl\": \"anime/pyat-nevest\"\n" + "  },\n" + "  {\n" + "    \"id\": \"2\",\n"
				+ "    \"fullUrl\": \"https://online.animedia.tv/anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove/1/1\",\n"
				+ "    \"rootUrl\": \"anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove\"\n" + "  }\n" + "]";
	}

	private String getSingleElementJson() {
		return "{\"id\":\"1\",\"fullUrl\":\"https://online.animedia.tv/anime/pyat-nevest/1/1\",\"rootUrl\":\"anime/pyat-nevest\"}";
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class Anime {

		/**
		 * Anime id in the local list
		 */
		private String id;

		/**
		 * Anime full url http://online.animedia.tv/anime/realnaya-devushka/1/1
		 */
		private String fullUrl;

		/**
		 * Anime root url anime/realnaya-devushka
		 */
		private String rootUrl;
	}
}