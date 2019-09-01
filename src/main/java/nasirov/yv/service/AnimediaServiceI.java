package nasirov.yv.service;

import java.util.List;
import java.util.Set;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.animedia.AnimediaTitleSearchInfo;

/**
 * Created by nasirov.yv
 */
public interface AnimediaServiceI {

	Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromAnimedia();

	Set<AnimediaTitleSearchInfo> getAnimediaSearchListFromGitHub();

	List<AnimediaMALTitleReferences> getCurrentlyUpdatedTitles();

	List<AnimediaMALTitleReferences> checkCurrentlyUpdatedTitles(List<AnimediaMALTitleReferences> fresh, List<AnimediaMALTitleReferences> fromCache);
}
