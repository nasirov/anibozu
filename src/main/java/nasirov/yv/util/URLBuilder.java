package nasirov.yv.util;

import static nasirov.yv.enums.Constants.FIRST_EPISODE;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

/**
 * Created by nasirov.yv
 */
public class URLBuilder {

	private URLBuilder() {

	}

	public static String build(String url, String dataList, @Nullable String firstEpisodeInSeason, @Nullable String numberOfEpisodesInSeason) {
		String episode = null;
		if (firstEpisodeInSeason != null) {
			episode = firstEpisodeInSeason;
		} else if (numberOfEpisodesInSeason != null) {
			episode = episodeChecker(numberOfEpisodesInSeason);
		}
		return url + "/" + dataList + "/" + episode;
	}

	public static String build(String url, @NotNull Map<String, String> queryParams) {
		return url + addQueryParametersToURL(queryParams);
	}

	private static String addQueryParametersToURL(Map<String, String> queryParams) {
		int count = 1;
		int size = queryParams.size();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("?");
		for (Map.Entry<String, String> queryParamsEntry : queryParams.entrySet()) {
			stringBuilder.append(queryParamsEntry.getKey()).append("=").append(queryParamsEntry.getValue());
			if (count < size) {
				stringBuilder.append("&");
			}
			count++;
		}
		return stringBuilder.toString();
	}

	private static String episodeChecker(String episode) {
		Pattern pattern = Pattern.compile("(\\d{1,3}-(\\d{1,3}|[xX]{1,3}))");
		Matcher matcher = pattern.matcher(episode);
		if (matcher.find()) {
			String[] range = episode.split("-");
			return range[0];
		}
		return FIRST_EPISODE.getDescription();
	}
}
