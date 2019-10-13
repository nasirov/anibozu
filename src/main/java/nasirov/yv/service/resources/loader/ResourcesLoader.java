package nasirov.yv.service.resources.loader;

import lombok.RequiredArgsConstructor;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.ReferencesServiceI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.resourcesLoader-enabled", havingValue = "true")
public class ResourcesLoader implements ResourcesLoaderI {

	private final ReferencesServiceI referencesManager;

	private final AnimediaServiceI animediaService;

	@Override
	@EventListener(ApplicationReadyEvent.class)
	public void loadAll() {
		loadMultiSeasonsReferences();
		loadAnimediaSearchInfoList();
	}

	private void loadMultiSeasonsReferences() {
		referencesManager.getMultiSeasonsReferences();
	}

	private void loadAnimediaSearchInfoList() {
		animediaService.getAnimediaSearchListFromAnimedia();
		animediaService.getAnimediaSearchListFromGitHub();
	}
}
