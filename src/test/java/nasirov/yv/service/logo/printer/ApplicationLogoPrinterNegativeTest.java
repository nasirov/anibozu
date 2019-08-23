package nasirov.yv.service.logo.printer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Created by nasirov.yv
 */
@TestPropertySource(properties = "application.services.applicationLogoPrinter-enabled=false")
public class ApplicationLogoPrinterNegativeTest extends AbstractApplicationLogoPrinterTest {

	@Test
	public void printApplicationLogoTestWhenDisabled() throws Exception {
		String applicationLogo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		assertEquals(0, logoFromBean.length());
		verify(mockPrintStream, never()).println(applicationLogo);
	}
}
