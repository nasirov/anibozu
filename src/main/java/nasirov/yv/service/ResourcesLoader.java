package nasirov.yv.service;

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

import java.util.LinkedHashSet;
import java.util.Set;

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
	
	private RoutinesIO routinesIO;
	
	@Autowired
	public ResourcesLoader(ReferencesManager referencesManager,
						   CacheManager cacheManager,
						   RoutinesIO routinesIO) {
		this.referencesManager = referencesManager;
		this.cacheManager = cacheManager;
		this.routinesIO = routinesIO;
	}
	
	@LoadResources
	public void loadMultiSeasonsReferences() {
		referencesManager.getMultiSeasonsReferences();
	}
	
	@LoadResources
	public void loadAnimediaSearchInfoList() {
		Cache animediaSearchListCache = cacheManager.getCache("animediaSearchListCache");
		Set<AnimediaTitleSearchInfo> animediaSearchList = routinesIO.unmarshalFromResource(resourceAnimediaSearchList, AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		animediaSearchListCache.put(animediaSearchListCacheName, animediaSearchList);
	}
}
