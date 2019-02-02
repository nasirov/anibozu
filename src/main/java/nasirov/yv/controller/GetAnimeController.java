package nasirov.yv.controller;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.enums.AnimeTypeOnAnimedia;
import nasirov.yv.serialization.Anime;
import nasirov.yv.service.AnimediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;

/**
 * Created by Хикка on 31.01.2019.
 */
@RestController
@RequestMapping("/anime")
@Slf4j
public class GetAnimeController {
	@Value("${cache.sortedAnimediaSearchList.name}")
	private String sortedAnimediaSearchListCacheName;
	
	private CacheManager cacheManager;
	
	private AnimediaService animediaService;
	
	@Autowired
	public GetAnimeController(CacheManager cacheManager, AnimediaService animediaService) {
		this.cacheManager = cacheManager;
		this.animediaService = animediaService;
	}
	
	@GetMapping("/getSortedAnime")
	public ResponseEntity<String> getSortedAnime() {
		try {
			animediaService.getSortedForSeasonAnime(animediaService.getAnimediaSearchList());
			return new ResponseEntity<>("Ok", HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/getMultiSeason")
	public ResponseEntity<Iterable<Anime>> getMultiSeason() {
		try {
			Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
			return new ResponseEntity<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.MULTISEASONS.getDescription(), LinkedHashSet.class), HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new ResponseEntity<>(new LinkedHashSet<>(), HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/getSingleSeason")
	public ResponseEntity<Iterable<Anime>> getSingleSeason() {
		try {
			Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
			return new ResponseEntity<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.SINGLESEASON.getDescription(), LinkedHashSet.class), HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new ResponseEntity<>(new LinkedHashSet<>(), HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/getAnnouncement")
	public ResponseEntity<Iterable<Anime>> getAnnouncement() {
		try {
			Cache sortedAnimediaSearchListCache = cacheManager.getCache(sortedAnimediaSearchListCacheName);
			return new ResponseEntity<>(sortedAnimediaSearchListCache.get(AnimeTypeOnAnimedia.ANNOUNCEMENT.getDescription(), LinkedHashSet.class), HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return new ResponseEntity<>(new LinkedHashSet<>(), HttpStatus.BAD_REQUEST);
	}
}
