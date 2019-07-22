package nasirov.yv.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.AbstractTest;
import nasirov.yv.service.context.LogoPrinterContextListener;
import nasirov.yv.util.RoutinesIO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ApplicationLogoPrinter.class, LogoPrinterContextListener.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Slf4j
@TestPropertySource(locations = "classpath:testSystem.properties")
public class ApplicationLogoPrinterTest extends AbstractTest {

	private static PrintStream mockPrintStream = mock(PrintStream.class);

	private static StringBuilder logoFromBean = new StringBuilder();

	static {
		setTestVars();
	}

	@Value("classpath:${resources.applicationLogo.name}")
	private Resource resourcesApplicationLogo;


	private static void setTestVars() {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(ResourceUtils.getFile("classpath:system.properties")));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		String applicationLogo = RoutinesIO.readFromFile("classpath:" + properties.getProperty("resources.applicationLogo.name"));
		System.setOut(mockPrintStream);
		doAnswer(x -> {
			logoFromBean.append((String) x.getArgument(0));
			return Void.TYPE;
		}).when(mockPrintStream).println(applicationLogo);
	}
	@Test
	public void printApplicationLogoTest() throws Exception {
		String applicationLogo = RoutinesIO.readFromResource(resourcesApplicationLogo);
		assertEquals(applicationLogo, logoFromBean.toString());
		verify(mockPrintStream, times(1)).println(applicationLogo);
	}
}