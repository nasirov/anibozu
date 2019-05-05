package nasirov.yv.service.context;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.AbstractTest;
import nasirov.yv.service.ApplicationLogoPrinter;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.ResourceUtils;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ApplicationLogoPrinter.class, LogoPrinterContextListener.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class LogoPrinterContextListenerTest extends AbstractTest {

	private static PrintStream mockPrintStream = mock(PrintStream.class);

	static {
		setTestVars();
	}

	@Value("classpath:${resources.applicationLogo.name}")
	private Resource resourcesApplicationLogo;

	@Autowired
	private ApplicationLogoPrinter applicationLogoPrinter;

	private static void setTestVars() {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(ResourceUtils.getFile("classpath:system.properties")));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		String appLogo = RoutinesIO.readFromFile("classpath:" + properties.getProperty("resources.applicationLogo.name"));
		System.setOut(mockPrintStream);
		doThrow(RuntimeException.class).when(mockPrintStream).println(eq(appLogo));
	}

	@Test(expected = RuntimeException.class)
	public void onApplicationEventTestException() {
		String appLogo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		verify(mockPrintStream, times(1)).println(eq(appLogo));
		applicationLogoPrinter.printApplicationLogo();
	}
}