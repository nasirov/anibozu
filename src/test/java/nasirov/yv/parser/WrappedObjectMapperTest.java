package nasirov.yv.parser;

import nasirov.yv.AbstractTest;
import nasirov.yv.serialization.Anime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {WrappedObjectMapper.class})
public class WrappedObjectMapperTest extends AbstractTest {
	@Autowired
	private WrappedObjectMapper wrappedObjectMapper;
	
	@Test
	public void unmarshalCollectionNullValue() throws Exception {
		assertNull(wrappedObjectMapper.unmarshal("", String.class, List.class));
	}
	
	@Test
	public void unmarshalNullValue() throws Exception {
		assertNull(wrappedObjectMapper.unmarshal("", String.class));
	}
	
	@Test
	public void unmarshalCollection() throws Exception {
		List<Anime> unmarshal = wrappedObjectMapper.unmarshal(getCollectionJson(), Anime.class, ArrayList.class);
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
	
	@Test
	public void unmarshalSingleElement() throws Exception {
		Anime singleElement = wrappedObjectMapper.unmarshal(getSingleElementJson(), Anime.class);
		assertNotNull(singleElement);
		assertEquals("1", singleElement.getId());
		assertEquals("https://online.animedia.tv/anime/pyat-nevest/1/1", singleElement.getFullUrl());
		assertEquals("anime/pyat-nevest", singleElement.getRootUrl());
	}
	
	@Test(expected = NullPointerException.class)
	public void marshalNPE() {
		wrappedObjectMapper.marshal(null, null);
	}
	
	@Test
	public void marshalException() throws IOException {
		File tempDir = new File(tempFolderName + File.separator + "test.txt");
		assertFalse(tempDir.exists());
		Anime forMarshal = new Anime("1", "https://online.animedia.tv/anime/pyat-nevest/1/1", "anime/pyat-nevest");
		wrappedObjectMapper.marshal(tempDir, forMarshal);
		assertFalse(tempDir.exists());
	}
	
	@Test
	public void marshal() throws Exception {
		File tempFile = new File("test.txt");
		assertFalse(tempFile.exists());
		Anime forMarshal = new Anime("1", "https://online.animedia.tv/anime/pyat-nevest/1/1", "anime/pyat-nevest");
		wrappedObjectMapper.marshal(tempFile, forMarshal);
		assertTrue(tempFile.exists());
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile))) {
			String buf;
			while ((buf = bufferedReader.readLine()) != null) {
				stringBuilder.append(buf);
			}
		}
		FileSystemUtils.deleteRecursively(tempFile);
		assertEquals(getSingleElementJson(), stringBuilder.toString());
		assertFalse(tempFile.exists());
	}
	
	private String getCollectionJson() {
		return "[\n" +
				"  {\n" +
				"    \"id\": \"1\",\n" +
				"    \"fullUrl\": \"https://online.animedia.tv/anime/pyat-nevest/1/1\",\n" +
				"    \"rootUrl\": \"anime/pyat-nevest\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"id\": \"2\",\n" +
				"    \"fullUrl\": \"https://online.animedia.tv/anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove/1/1\",\n" +
				"    \"rootUrl\": \"anime/domashnij-pitomec-inogda-sidyaschij-na-moej-golove\"\n" +
				"  }\n" +
				"]";
	}
	
	private String getSingleElementJson() {
		return "{\"id\":\"1\",\"fullUrl\":\"https://online.animedia.tv/anime/pyat-nevest/1/1\",\"rootUrl\":\"anime/pyat-nevest\"}";
	}
}