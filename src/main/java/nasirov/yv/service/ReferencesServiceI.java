package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface ReferencesServiceI {

	Set<TitleReference> getReferences();

	void updateReferences(Set<TitleReference> references);

	Set<TitleReference> getMatchedReferences(Set<UserMALTitleInfo> watchingTitles, Set<TitleReference> references);
}
