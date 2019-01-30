package nasirov.yv.service;

import nasirov.yv.service.annotation.LoadResources;
import nasirov.yv.util.ReferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Хикка on 23.01.2019.
 */
@Service
@LoadResources
public class ResourcesLoader {
	private static final Logger logger = LoggerFactory.getLogger(ResourcesLoader.class);
	
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
