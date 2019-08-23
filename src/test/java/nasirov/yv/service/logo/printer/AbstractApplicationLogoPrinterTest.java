package nasirov.yv.service.logo.printer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.PrintStream;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.AbstractTest;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {ApplicationLogoPrinterConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Slf4j
@PropertySource("classpath:application.yml")
public abstract class AbstractApplicationLogoPrinterTest extends AbstractTest {

	protected static PrintStream mockPrintStream;

	protected static StringBuilder logoFromBean;

	private static String APPLICATION_LOGO = "anime-checker-logo.txt";

	static {
		mockPrintStream = mock(PrintStream.class);
		logoFromBean = new StringBuilder();
		setTestVars();
	}

	@Value("classpath:${application.resources.applicationLogo}")
	protected Resource resourcesApplicationLogo;


	private static void setTestVars() {
		String applicationLogo = RoutinesIO.readFromFile("classpath:" + APPLICATION_LOGO);
		System.setOut(mockPrintStream);
		doAnswer(x -> {
			logoFromBean.append((String) x.getArgument(0));
			return Void.TYPE;
		}).when(mockPrintStream).println(applicationLogo);
	}
}
