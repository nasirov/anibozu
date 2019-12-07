package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;

/**
 * Created by nasirov.yv
 */
public interface MALServiceI {

	Set<UserMALTitleInfo> getWatchingTitles(String username)
			throws WatchingTitlesNotFoundException, MALUserAccountNotFoundException, MALUserAnimeListAccessException;

	Set<UserMALTitleInfo> getWatchingTitlesWithUpdatedNumberOfWatchedEpisodes(Set<UserMALTitleInfo> watchingTitlesNew,
			Set<UserMALTitleInfo> watchingTitlesFromCache);

	boolean isWatchingTitlesUpdated(Set<UserMALTitleInfo> watchingTitlesNew, Set<UserMALTitleInfo> watchingTitlesFromCache);

	boolean isTitleExist(String titleOnMAL);
}
