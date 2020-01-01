package nasirov.yv.parser;

import static nasirov.yv.utils.IOUtils.readFromFile;
import static nasirov.yv.utils.IOUtils.unmarshal;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.parser.impl.WrappedObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Created by nasirov.yv
 */
public class WrappedObjectMapperTest {

	@Rule
	public final TemporaryFolder tempFile = new TemporaryFolder();

	private final WrappedObjectMapperI wrappedObjectMapperI = new WrappedObjectMapper(new ObjectMapper());

	@Test
	public void marshalSingleToFileOk() {
		FooBarObject fooBarObject = new FooBarObject("foo");
		wrappedObjectMapperI.marshal(getResultFile(), fooBarObject);
		assertEquals(fooBarObject, unmarshal(readFromFile(getResultFile().getAbsolutePath()), FooBarObject.class));
	}

	@Test
	public void marshalCollectionToFileOk() {
		List<FooBarObject> fooBarList = Lists.newArrayList(new FooBarObject("foo"), new FooBarObject("bar"));
		wrappedObjectMapperI.marshal(getResultFile(), fooBarList);
		assertEquals(fooBarList, unmarshal(readFromFile(getResultFile().getAbsolutePath()), FooBarObject.class, ArrayList.class));
	}

	private File getResultFile() {
		return new File(tempFile.getRoot(), "fooBar.json");
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class FooBarObject {

		private String testField;
	}
}