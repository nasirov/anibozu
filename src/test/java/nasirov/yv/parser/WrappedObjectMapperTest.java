package nasirov.yv.parser;

import static nasirov.yv.utils.IOUtils.readFromFile;
import static nasirov.yv.utils.IOUtils.unmarshal;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import nasirov.yv.parser.impl.WrappedObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Created by nasirov.yv
 */
public class WrappedObjectMapperTest {

	@Rule
	public final TemporaryFolder tempFile = new TemporaryFolder();

	private WrappedObjectMapperI wrappedObjectMapper;

	private ObjectMapper objectMapper;

	@Before
	public void setUp() {
		objectMapper = Mockito.spy(ObjectMapper.class);
		wrappedObjectMapper = new WrappedObjectMapper(objectMapper);
	}

	@Test
	public void marshalSingleToFileOk() {
		FooBarObject fooBarObject = buildFooBarObject("foo");
		wrappedObjectMapper.marshal(getResultFile(), fooBarObject);
		assertEquals(fooBarObject, unmarshal(readFromFile(getResultFile().getAbsolutePath()), FooBarObject.class));
	}

	@Test
	public void marshalCollectionToFileOk() {
		List<FooBarObject> fooBarList = buildTestList();
		wrappedObjectMapper.marshal(getResultFile(), fooBarList);
		assertEquals(fooBarList, unmarshal(readFromFile(getResultFile().getAbsolutePath()), FooBarObject.class, ArrayList.class));
	}

	@Test(expected = JsonGenerationException.class)
	public void marshalFail() {
		FooBarObject fooBarObject = buildFooBarObject("foo");
		mockObjectMapperWriteFail(fooBarObject);
		wrappedObjectMapper.marshal(getResultFile(), fooBarObject);
	}

	@Test
	public void unmarshalListOk() {
		String jsonList = "[{\"testField\":\"foo\"},{\"testField\":\"bar\"}]";
		List<FooBarObject> result = wrappedObjectMapper.unmarshalToList(jsonList, FooBarObject.class, ArrayList.class);
		assertEquals(result, buildTestList());
	}

	@Test
	public void unmarshalSetOk() {
		String jsonList = "[{\"testField\":\"foo\"},{\"testField\":\"bar\"}]";
		Set<FooBarObject> result = wrappedObjectMapper.unmarshalToSet(jsonList, FooBarObject.class, LinkedHashSet.class);
		assertEquals(result, buildTestSet());
	}

	@Test(expected = JsonGenerationException.class)
	public void unmarshalListFail() {
		mockObjectMapperReadFail();
		wrappedObjectMapper.unmarshalToList("", FooBarObject.class, ArrayList.class);
	}

	@Test(expected = JsonGenerationException.class)
	public void unmarshalSetFail() {
		mockObjectMapperReadFail();
		wrappedObjectMapper.unmarshalToSet("", FooBarObject.class, LinkedHashSet.class);
	}

	@SneakyThrows
	private void mockObjectMapperWriteFail(Object content) {
		doThrow(new JsonGenerationException("some JsonGenerationException", new RuntimeException(), null)).when(objectMapper)
				.writeValue(getResultFile(), content);
	}

	@SneakyThrows
	private void mockObjectMapperReadFail() {
		doThrow(new JsonGenerationException("some JsonGenerationException", new RuntimeException(), null)).when(objectMapper)
				.readValue(eq(""), any(CollectionType.class));
	}

	private File getResultFile() {
		return new File(tempFile.getRoot(), "fooBar.json");
	}

	private FooBarObject buildFooBarObject(String testFieldValue) {
		return new FooBarObject(testFieldValue);
	}

	private List<FooBarObject> buildTestList() {
		return Lists.newArrayList(buildFooBarObject("foo"), buildFooBarObject("bar"));
	}

	private Set<FooBarObject> buildTestSet() {
		return Sets.newLinkedHashSet(buildTestList());
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class FooBarObject {

		private String testField;
	}
}