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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.animedia-service-source", havingValue = "api")
public class AnimediaApiService implements AnimediaServiceI {

	private final AnimediaApiFeignClient animediaApiFeignClient;

	@Override
	public Set<AnimediaSearchListTitle> getAnimediaSearchList() {
		return Sets.union(animediaApiFeignClient.getAnimediaSearchList("1"), animediaApiFeignClient.getAnimediaSearchList("2"));
	}

	@Override
	public Response getDataLists(AnimediaSearchListTitle animediaSearchTitle) {
		return Iterables.get(animediaApiFeignClient.getTitleInfo(animediaSearchTitle.getAnimeId())
				.getResponse(), 0);
	}

	@Override
	public List<Response> getDataListEpisodes(String animeId, String dataList) {
		return animediaApiFeignClient.getDataListInfo(animeId, dataList)
				.getResponse();
	}
}
