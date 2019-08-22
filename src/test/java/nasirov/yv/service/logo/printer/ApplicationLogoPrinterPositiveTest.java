package nasirov.yv.service.logo.printer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Created by nasirov.yv
 */

@TestPropertySource(properties = "service.applicationLogoPrinter.enabled=true")
public class ApplicationLogoPrinterPositiveTest extends AbstractApplicationLogoPrinterTest {

	@Test
	public void printApplicationLogoTestWhenEnabled() throws Exception {
		String applicationLogo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		assertEquals(applicationLogo, logoFromBean.toString());
		verify(mockPrintStream, times(1)).println(applicationLogo);
	}
}