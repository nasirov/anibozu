package nasirov.yv.service;

import java.util.Map;
import java.util.Set;
import nasirov.yv.data.animedia.Anime;
import nasirov.yv.data.animedia.AnimeTypeOnAnimedia;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;

/**
 * Created by nasirov.yv
 */
public interface ResourcesServiceI {

	Map<AnimeTypeOnAnimedia, Set<Anime>> getAnimeSortedByType(Set<AnimediaTitleSearchInfo> animediaSearchListInput);

	Map<AnimeTypeOnAnimedia, Set<Anime>> getAnimeSortedByTypeFromCache();

	Set<AnimediaTitleSearchInfo> checkSortedAnime(Map<AnimeTypeOnAnimedia, Set<Anime>> allTypes, Set<AnimediaTitleSearchInfo> animediaSearchList);

	boolean isAnimediaSearchListFromGitHubUpToDate(Set<AnimediaTitleSearchInfo> fromGitHub, Set<AnimediaTitleSearchInfo> fromAnimedia);

	boolean isAllSingleSeasonAnimeHasConcretizedMALTitleName(Set<Anime> singleSeasonAnime,
			Set<AnimediaTitleSearchInfo> animediaSearchListFromResources);

	boolean isReferencesAreFull(Set<Anime> multiSeasonsAnime, Set<AnimediaMALTitleReferences> allReferences);
}
