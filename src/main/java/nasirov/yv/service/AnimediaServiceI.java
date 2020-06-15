package nasirov.yv.service;

import java.util.List;
import java.util.Set;
import nasirov.yv.data.fandub.animedia.AnimediaSearchListTitle;
import org.springframework.cache.annotation.Cacheable;

/**
 * Created by nasirov.yv
 */
public interface AnimediaServiceI {

	/**
	 * Searches for an anime list from animedia API
	 *
	 * @return list with title search info on animedia
	 */
	Set<AnimediaSearchListTitle> getAnimediaSearchList();

	/**
	 * Searches for title info by anime id
	 *
	 * @param animediaSearchTitle an animedia search title
	 * @return title info
	 */
	List<String> getDataLists(AnimediaSearchListTitle animediaSearchTitle);

	/**
	 * Searches for a data list episodes
	 *
	 * @param animeId  title id for search
	 * @param dataList data list number
	 * @return list of episodes info
	 */
	@Cacheable(value = "dataListInfo", key = "T(java.lang.String).valueOf(T(java.util.Objects).hash(#animeId, #dataList))")
	List<String> getEpisodes(String animeId, String dataList);
}
