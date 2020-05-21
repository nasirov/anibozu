package nasirov.yv.service;

import java.util.List;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.exception.mal.MALUserAccountNotFoundException;
import nasirov.yv.exception.mal.MALUserAnimeListAccessException;
import nasirov.yv.exception.mal.WatchingTitlesNotFoundException;

/**
 * Created by nasirov.yv
 */
public interface MALServiceI {

	List<MalTitle> getWatchingTitles(String username)
			throws WatchingTitlesNotFoundException, MALUserAccountNotFoundException, MALUserAnimeListAccessException;

	boolean isTitleExist(String titleNameOnMal, Integer titleIdOnMal);
}
