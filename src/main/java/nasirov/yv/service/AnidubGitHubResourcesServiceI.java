package nasirov.yv.service;


import java.util.Map;
import nasirov.yv.data.anidub.AnidubTitle;

/**
 * Created by nasirov.yv
 */
public interface AnidubGitHubResourcesServiceI {

	Map<Integer, AnidubTitle> getAnidubTitles();
}
