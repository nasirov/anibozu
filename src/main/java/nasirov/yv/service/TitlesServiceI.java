package nasirov.yv.service;


import java.util.List;
import java.util.Map;
import nasirov.yv.data.github.GitHubResource;
import nasirov.yv.data.mal.UserMALTitleInfo;

/**
 * Created by nasirov.yv
 */
public interface TitlesServiceI<T extends GitHubResource> {

	/**
	 * Groups a subclass of {@link GitHubResource} by {@link GitHubResource#getTitleIdOnMal()} for O(1) search by {@link UserMALTitleInfo#getAnimeId()}
	 *
	 * @return a map which key - {@link GitHubResource#getTitleIdOnMal()}, value - list of titles with matched {@link GitHubResource#getTitleIdOnMal()}
	 */
	Map<Integer, List<T>> getTitles();
}
