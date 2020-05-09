package nasirov.yv.service;


import java.util.Map;
import nasirov.yv.data.anidub.api.AnidubApiTitle;
import nasirov.yv.data.anidub.site.AnidubSiteTitle;

/**
 * Created by nasirov.yv
 *
 * @param <T> anidub dto type {@link AnidubApiTitle} or {@link AnidubSiteTitle}
 */
public interface AnidubGitHubResourcesServiceI<T> {

	Map<Integer, T> getAnidubTitles();
}
