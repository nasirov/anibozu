package nasirov.yv.service.logo.printer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.AbstractTest;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.ResourceUtils;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ApplicationLogoPrinterConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractApplicationLogoPrinterTest extends AbstractTest {

	protected static PrintStream mockPrintStream;

	protected static StringBuilder logoFromBean;

	static {
		mockPrintStream = mock(PrintStream.class);
		logoFromBean = new StringBuilder();
		setTestVars();
	}

	@Value("classpath:${resources.applicationLogo.name}")
	protected Resource resourcesApplicationLogo;


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
}
