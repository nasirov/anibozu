package nasirov.yv.service.impl;

import static nasirov.yv.data.constants.BaseConstants.JOINED_EPISODE_REGEXP;
import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedAndOngoing;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;
import static nasirov.yv.util.AnimediaUtils.isTitleUpdated;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.animedia.api.Response;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.http.feign.GitHubFeignClient;
import nasirov.yv.parser.AnimediaHTMLParserI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.ReferencesServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReferencesService implements ReferencesServiceI {

	private final GitHubFeignClient gitHubFeignClient;

	private final AnimediaHTMLParserI animediaHTMLParser;

	private final AnimediaServiceI animediaService;

	private final GitHubAuthProps gitHubAuthProps;

	/**
	 * Searches for references which extracted to GitHub
	 *
	 * @return animedia references
	 */
	@Override
	@Cacheable(value = "github", key = "'references'", unless = "#result?.isEmpty()")
	public Set<TitleReference> getReferences() {
		return Sets.newLinkedHashSet(gitHubFeignClient.getReferences("token " + gitHubAuthProps.getToken()));
	}

	/**
	 * Updates given references
	 *
	 * @param references references for update
	 */
	@Override
	public void updateReferences(Set<TitleReference> references) {
		references.stream()
				.filter(this::isReferenceNeedUpdate)
				.forEach(this::handleReference);
	}

	private boolean isReferenceNeedUpdate(TitleReference reference) {
		return !(isTitleUpdated(reference) || isTitleNotFoundOnMAL(reference));
	}

	private void handleReference(TitleReference reference) {
		List<Response> episodesList = animediaService.getDataListInfo(reference.getAnimeIdOnAnimedia(), reference.getDataListOnAnimedia());
		if (episodesList.isEmpty()) {
			return;
		}
		List<String> episodesRange = episodesList.stream()
				.map(x -> animediaHTMLParser.extractEpisodeNumber(x.getEpisodeName()))
				.collect(Collectors.toList());
		if (isTitleConcretizedAndOngoing(reference)) {
			enrichConcretizedAndOngoingReference(reference, episodesRange);
		} else {
			enrichRegularReference(reference, episodesRange);
		}
	}

	/**
	 * Searches for references based on user watching titles
	 *
	 * @param watchingTitles user watching titles from MAL
	 * @return matched references
	 */
	@Override
	public Set<TitleReference> getMatchedReferences(Set<UserMALTitleInfo> watchingTitles, Set<TitleReference> references) {
		return watchingTitles.stream()
				.map(x -> findMatchedReference(x, references))
				.flatMap(List::stream)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private List<TitleReference> findMatchedReference(UserMALTitleInfo userMALTitleInfo, Set<TitleReference> references) {
		return references.stream()
				.filter(x -> x.getTitleNameOnMAL()
						.equals(userMALTitleInfo.getTitle()))
				.map(x -> setPosterUrlFromMAL(userMALTitleInfo, x))
				.collect(Collectors.toList());
	}

	private TitleReference setPosterUrlFromMAL(UserMALTitleInfo userMALTitleInfo, TitleReference reference) {
		reference.setPosterUrlOnMAL(userMALTitleInfo.getPosterUrl());
		return reference;
	}

	private void enrichRegularReference(TitleReference reference, List<String> episodesList) {
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesList));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
		//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 51 из 100
		reference.setMinOnAnimedia(correctFirstEpisodeAndMin);
		// TODO: 17.12.2019 uncomment and implement when animedia improve api object that will return max episode in season
//		reference.setMaxConcretizedEpisodeOnAnimedia("correctMaxInDataList");
		episodesList.stream()
				.filter(x -> x.matches(JOINED_EPISODE_REGEXP))
				.findFirst()
				.ifPresent(x -> reference.setEpisodesRangeOnAnimedia(episodesList));
		reference.setCurrentMaxOnAnimedia(correctCurrentMax);
	}

	private void enrichConcretizedAndOngoingReference(TitleReference reference, List<String> episodesList) {
		String currentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		reference.setCurrentMaxOnAnimedia(currentMax);
	}
}
