package nasirov.yv.service.impl.common;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.service.CacheFillerServiceI;
import nasirov.yv.service.TitlesServiceI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.cacheFillerService.enabled", havingValue = "true")
public class CacheFillerService implements CacheFillerServiceI {

	private final TitlesServiceI titlesServiceI;

	@Override
	@EventListener(classes = ApplicationReadyEvent.class)
	public void fillGithubCache() {
		log.info("Trying to fill github cache...");
		Arrays.stream(FanDubSource.values())
				.forEach(titlesServiceI::getTitles);
		log.info("Github cache was filled successfully.");
	}
}
