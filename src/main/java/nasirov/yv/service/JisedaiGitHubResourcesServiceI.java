package nasirov.yv.service;


import java.util.Map;

/**
 * Created by nasirov.yv
 *
 * @param <T> jisedai dto type
 */
public interface JisedaiGitHubResourcesServiceI<T> {

	Map<Integer, T> getJisedaiTitles();
}
