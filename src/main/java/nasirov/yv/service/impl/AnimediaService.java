package nasirov.yv.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.api.Response;
import nasirov.yv.http.feign.AnimediaApiFeignClient;
import nasirov.yv.service.AnimediaServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaService implements AnimediaServiceI {

	private final AnimediaApiFeignClient animediaApiFeignClient;

	/**
	 * Searches for an anime list from animedia API
	 *
	 * @return list with title search info on animedia
	 */
	@Override
	@Cacheable(value = "animeList", key = "'animeList'")
	public Set<AnimediaSearchListTitle> getAnimediaSearchList() {
		return Sets.union(animediaApiFeignClient.getAnimediaSearchList("1"), animediaApiFeignClient.getAnimediaSearchList("2"));
	}

	/**
	 * Searches for title info by anime id
	 *
	 * @param animeId title id for search
	 * @return title info
	 */
	@Override
	@Cacheable(value = "titleInfo", key = "#animeId")
	public Response getTitleInfo(String animeId) {
		return Iterables.get(animediaApiFeignClient.getTitleInfo(animeId)
				.getResponse(), 0);
	}

	/**
	 * Searched for data list info by anime id and data list number
	 *
	 * @param animeId  title id for search
	 * @param dataList data list number
	 * @return list of episodes info
	 */
	@Override
	@Cacheable(value = "dataListInfo", key = "T(java.lang.String).valueOf(T(java.util.Objects).hash(#animeId, #dataList))")
	public List<Response> getDataListInfo(String animeId, String dataList) {
		return animediaApiFeignClient.getDataListInfo(animeId, dataList)
				.getResponse();
	}
}
