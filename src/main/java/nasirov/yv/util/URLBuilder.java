package nasirov.yv.util;

import static nasirov.yv.data.enums.Constants.FIRST_EPISODE;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriUtils;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class URLBuilder {

	private static final Pattern EPISODES_RANGE_PATTERN = Pattern.compile("(\\d{1,3}-(\\d{1,3}|[xXхХ]{1,3}))");

	public static String build(String url, String dataList, String firstEpisodeInSeason, String numberOfEpisodesInSeason) {
		String episode = null;
		if (firstEpisodeInSeason != null) {
			episode = firstEpisodeInSeason;
		} else if (numberOfEpisodesInSeason != null) {
			episode = episodeChecker(numberOfEpisodesInSeason);
		}
		return url + "/" + dataList + "/" + episode;
	}

	public static String build(String url, Map<String, String> queryParams) {
		return url + (queryParams.isEmpty() ? "" : addQueryParametersToURL(queryParams));
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
		return UriUtils.encodeQuery(stringBuilder.toString(), StandardCharsets.UTF_8);
	}

	private static String episodeChecker(String episode) {
		Matcher matcher = EPISODES_RANGE_PATTERN.matcher(episode);
		if (matcher.find()) {
			String[] range = episode.split("-");
			return range[0];
		}
		return FIRST_EPISODE.getDescription();
	}
}
