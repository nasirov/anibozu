package nasirov.yv.service.logo.printer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.PrintStream;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.util.RoutinesIO;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class ApplicationLogoPrinterTest {

	private static final String APPLICATION_LOGO_FILENAME = "anime-checker-logo.txt";

	private ApplicationLogoPrinterI applicationLogoPrinter;

	private StringBuilder logoFromBean;

	private String applicationLogo;

	@Before
	public void setUp() {
		ResourcesNames resourcesNames = mock(ResourcesNames.class);
		doReturn(APPLICATION_LOGO_FILENAME).when(resourcesNames).getApplicationLogo();
		applicationLogoPrinter = new ApplicationLogoPrinter(resourcesNames);
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
		applicationLogo = RoutinesIO.readFromFile("classpath:" + APPLICATION_LOGO_FILENAME);
		System.setOut(mockPrintStream);
		doAnswer(x -> {
			logoFromBean.append((String) x.getArgument(0));
			return x.callRealMethod();
		}).when(mockPrintStream).println(applicationLogo);
	}
}
