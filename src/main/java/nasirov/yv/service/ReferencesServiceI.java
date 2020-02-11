package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.TitleReference;

/**
 * Created by nasirov.yv
 */
public interface ReferencesServiceI {

	Set<TitleReference> getReferences();

	void updateReferences(Set<TitleReference> references);
}
