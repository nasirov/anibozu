package nasirov.yv.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;
import nasirov.yv.AbstractTest;
import nasirov.yv.AnimeCheckerApplication;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

/**
 * Created by nasirov.yv
 */
public class ApplicationLogoPrinterTest extends AbstractTest {

	@Value("classpath:${resources.applicationLogo.name}")
	private Resource resourcesApplicationLogo;

	private PrintStream mockPrintStream;


	@Test
	public void printApplicationLogo() throws Exception {
		assertTrue(resourcesApplicationLogo.exists());
		mockPrintStream = mock(PrintStream.class);
		System.setOut(mockPrintStream);
		String applicationLogo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		doNothing().when(mockPrintStream).println(applicationLogo);
		AnimeCheckerApplication.main(new String[0]);
		verify(mockPrintStream, times(1)).println(applicationLogo);
	}

}