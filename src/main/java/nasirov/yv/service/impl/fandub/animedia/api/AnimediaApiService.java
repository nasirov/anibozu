package nasirov.yv.service.impl.fandub.animedia.api;

import static java.util.Optional.ofNullable;
import static nasirov.yv.data.constants.ServiceSourceType.API;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.fandub.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.fandub.animedia.api.ApiEpisode;
import nasirov.yv.data.fandub.animedia.api.DataList;
import nasirov.yv.data.fandub.animedia.api.DataListInfoResponse;
import nasirov.yv.data.fandub.animedia.api.SearchListTitle;
import nasirov.yv.data.fandub.animedia.api.TitleInfo;
import nasirov.yv.data.fandub.animedia.api.TitleInfoResponse;
import nasirov.yv.http.feign.fandub.animedia.api.AnimediaApiFeignClient;
import nasirov.yv.service.AnimediaServiceI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.services.animedia-service-source", havingValue = API)
public class AnimediaApiService implements AnimediaServiceI {

	private final AnimediaApiFeignClient animediaApiFeignClient;

	@Override
	public Set<AnimediaSearchListTitle> getAnimediaSearchList() {
		return Sets.union(animediaApiFeignClient.getAnimediaSearchList("1"), animediaApiFeignClient.getAnimediaSearchList("2"))
				.stream()
				.map(this::buildAnimediaSearchListTitle)
				.collect(Collectors.toSet());
	}

	@Override
	public List<String> getDataLists(AnimediaSearchListTitle animediaSearchTitle) {
		TitleInfoResponse response = animediaApiFeignClient.getTitleInfo(animediaSearchTitle.getAnimeId());
		TitleInfo titleInfo = response.getTitleInfo();
		List<DataList> dataLists = titleInfo.getDataLists();
		return dataLists.stream()
				.map(DataList::getDataListId)
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getEpisodes(String animeId, String dataList) {
		DataListInfoResponse response = animediaApiFeignClient.getDataListInfo(animeId, dataList);
		List<ApiEpisode> episodes = response.getEpisodes();
		return episodes.stream()
				.map(ApiEpisode::getEpisodeName)
				.collect(Collectors.toList());
	}

	private AnimediaSearchListTitle buildAnimediaSearchListTitle(SearchListTitle searchListTitle) {
		return AnimediaSearchListTitle.builder()
				.animeId(searchListTitle.getAnimeId())
				.url(searchListTitle.getUrl())
				.dataLists(extractDataLists(searchListTitle))
				.build();
	}

	private List<String> extractDataLists(SearchListTitle searchListTitle) {
		return ofNullable(searchListTitle.getDataLists()).orElseGet(Collections::emptyList)
				.stream()
				.map(DataList::getDataListId)
				.collect(Collectors.toList());
	}
}
