package nasirov.yv.service.resources.loader;

import lombok.RequiredArgsConstructor;
import nasirov.yv.service.AnimediaService;
import nasirov.yv.service.ReferencesManager;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public class ResourcesLoader {

	private final ReferencesManager referencesManager;

	private final AnimediaService animediaService;

	@EventListener(ContextRefreshedEvent.class)
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
