package nasirov.yv.service;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.service.annotation.LoadResources;
import nasirov.yv.util.RoutinesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@LoadResources
@Slf4j
public class ResourcesLoader {

	@Value("${cache.animediaSearchList.name}")
	private String animediaSearchListCacheName;

	@Value("classpath:${resources.animediaSearchList.name}")
	private Resource resourceAnimediaSearchList;

	private ReferencesManager referencesManager;

	private CacheManager cacheManager;

	@Autowired
	public ResourcesLoader(ReferencesManager referencesManager, CacheManager cacheManager) {
		this.referencesManager = referencesManager;
		this.cacheManager = cacheManager;
	}

	@LoadResources
	public void loadMultiSeasonsReferences() {
		referencesManager.getMultiSeasonsReferences();
	}

	@LoadResources
	public void loadAnimediaSearchInfoList() {
		Cache animediaSearchListCache = cacheManager.getCache(animediaSearchListCacheName);
		Set<AnimediaTitleSearchInfo> animediaSearchList = RoutinesIO
				.unmarshalFromResource(resourceAnimediaSearchList, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		animediaSearchListCache.put(animediaSearchListCacheName, animediaSearchList);
	}
}
