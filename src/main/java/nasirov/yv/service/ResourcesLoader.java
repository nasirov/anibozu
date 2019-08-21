package nasirov.yv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.service.annotation.LoadResources;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@LoadResources
@Slf4j
@RequiredArgsConstructor
public class ResourcesLoader {

	private final ReferencesManager referencesManager;

	private final AnimediaService animediaService;

	@LoadResources
	public void loadMultiSeasonsReferences() {
		referencesManager.getMultiSeasonsReferences();
	}

	@LoadResources
	public void loadAnimediaSearchInfoList() {
		animediaService.getAnimediaSearchListFromAnimedia();
		animediaService.getAnimediaSearchListFromGitHub();
	}
}
