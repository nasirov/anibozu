package nasirov.yv.service;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.service.annotation.LoadResources;
import nasirov.yv.util.ReferencesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@LoadResources
@Slf4j
public class ResourcesLoader {
	private ReferencesManager referencesManager;
	
	private AnimediaService animediaService;
	
	@Autowired
	public ResourcesLoader(ReferencesManager referencesManager,
						   AnimediaService animediaService) {
		this.referencesManager = referencesManager;
		this.animediaService = animediaService;
	}
	
	@LoadResources
	public void loadMultiSeasonsReferences() {
		referencesManager.getMultiSeasonsReferences();
	}
	
	@LoadResources
	public void loadAnimediaSearchInfoList() {
		animediaService.getAnimediaSearchList();
	}
}
