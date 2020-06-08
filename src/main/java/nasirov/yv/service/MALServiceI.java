package nasirov.yv.service;

import java.util.List;
import nasirov.yv.data.mal.MalTitle;
import nasirov.yv.exception.mal.MalException;

/**
 * Created by nasirov.yv
 */
public interface MALServiceI {

	List<MalTitle> getWatchingTitles(String username) throws MalException;

	boolean isTitleExist(String titleNameOnMal, Integer titleIdOnMal);
}
