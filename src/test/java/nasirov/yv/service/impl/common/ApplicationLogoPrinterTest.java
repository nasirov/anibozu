package nasirov.yv.service.impl.common;

import static nasirov.yv.utils.IOUtils.readFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.PrintStream;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.service.ApplicationLogoPrinterI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Nasirov Yuriy
 */
class ApplicationLogoPrinterTest {

	private static final String APPLICATION_LOGO_FILENAME = "anime-checker-logo.txt";

	private ApplicationLogoPrinterI applicationLogoPrinter;

	private StringBuilder logoFromBean;

	private String applicationLogo;

	@BeforeEach
	void setUp() {
		ResourcesNames resourcesNames = mock(ResourcesNames.class);
		doReturn(APPLICATION_LOGO_FILENAME).when(resourcesNames)
				.getName();
		applicationLogoPrinter = new ApplicationLogoPrinter();
		ReflectionTestUtils.setField(applicationLogoPrinter, "applicationLogoResource", new ClassPathResource(APPLICATION_LOGO_FILENAME));
	}

	@Test
	void shouldPrintApplicationLogo() {
		//given
		mockPrintStream();
		//when
		applicationLogoPrinter.printApplicationLogo();
		//then
		assertEquals(applicationLogo, logoFromBean.toString());
	}

	private void mockPrintStream() {
		PrintStream mockPrintStream = mock(PrintStream.class);
		logoFromBean = new StringBuilder();
		applicationLogo = readFromFile("classpath:" + APPLICATION_LOGO_FILENAME);
		System.setOut(mockPrintStream);
		doAnswer(x -> {
			logoFromBean.append((String) x.getArgument(0));
			return x.callRealMethod();
		}).when(mockPrintStream)
				.println(applicationLogo);
	}
}
