package nasirov.yv.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintStream;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.parser.impl.WrappedObjectMapper;
import nasirov.yv.service.impl.ApplicationLogoPrinter;
import nasirov.yv.util.RoutinesIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Created by nasirov.yv
 */
public class ApplicationLogoPrinterTest {

	private static final String APPLICATION_LOGO_FILENAME = "anime-checker-logo.txt";

	private ApplicationLogoPrinterI applicationLogoPrinter;

	private StringBuilder logoFromBean;

	private String applicationLogo;

	private RoutinesIO routinesIO;

	@Before
	public void setUp() {
		ResourcesNames resourcesNames = mock(ResourcesNames.class);
		doReturn(APPLICATION_LOGO_FILENAME).when(resourcesNames).getApplicationLogo();
		WrappedObjectMapperI wrappedObjectMapperI = new WrappedObjectMapper(new ObjectMapper());
		routinesIO = new RoutinesIO(wrappedObjectMapperI);
		applicationLogoPrinter = new ApplicationLogoPrinter(routinesIO);
		ReflectionTestUtils.setField(applicationLogoPrinter, "applicationLogoResource", new ClassPathResource(APPLICATION_LOGO_FILENAME));
	}

	@Test
	public void printApplicationLogoTest() {
		mockPrintStream();
		applicationLogoPrinter.printApplicationLogo();
		assertEquals(applicationLogo, logoFromBean.toString());
	}

	private void mockPrintStream() {
		PrintStream mockPrintStream = mock(PrintStream.class);
		logoFromBean = new StringBuilder();
		applicationLogo = routinesIO.readFromFile("classpath:" + APPLICATION_LOGO_FILENAME);
		System.setOut(mockPrintStream);
		doAnswer(x -> {
			logoFromBean.append((String) x.getArgument(0));
			return x.callRealMethod();
		}).when(mockPrintStream).println(applicationLogo);
	}
}
