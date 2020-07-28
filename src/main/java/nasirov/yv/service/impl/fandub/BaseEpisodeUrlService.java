package nasirov.yv.service.impl.fandub;

import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FANDUB_SITE_URL;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nasirov.yv.data.constants.BaseConstants;
import nasirov.yv.data.github.GitHubResource;
import nasirov.yv.fandub.dto.mal.MalTitle;
import nasirov.yv.service.EpisodeUrlServiceI;
import nasirov.yv.service.TitlesServiceI;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public abstract class BaseEpisodeUrlService<T extends GitHubResource> implements EpisodeUrlServiceI {

	private final TitlesServiceI<T> titlesService;

	@Override
	public final String getEpisodeUrl(MalTitle watchingTitle) {
		return Optional.ofNullable(getMatchedTitles(watchingTitle))
				.map(x -> buildUrl(watchingTitle, x))
				.orElse(NOT_FOUND_ON_FANDUB_SITE_URL);
	}

	/**
	 * Builds a fandub url for a watching title based on matched titles
	 *
	 * @param watchingTitle a watching title from mal
	 * @param matchedTitles not blank titles list
	 * @return if a new episode is available - a fandub url, not available - {@link BaseConstants#FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE}
	 */
	protected abstract String buildUrl(MalTitle watchingTitle, List<T> matchedTitles);

	private List<T> getMatchedTitles(MalTitle watchingTitle) {
		return titlesService.getTitles()
				.get(watchingTitle.getId());
	}
}
