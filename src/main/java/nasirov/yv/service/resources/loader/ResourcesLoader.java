package nasirov.yv.service.resources.loader;

import lombok.RequiredArgsConstructor;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.ReferencesServiceI;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public class ResourcesLoader {

	private final ReferencesServiceI referencesManager;

	private final AnimediaServiceI animediaService;

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
