package nasirov.yv.util;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nasirov.yv.enums.Constants.FIRST_EPISODE;

/**
 * Created by nasirov.yv
 */
@Component
public class URLBuilder {
	public String build(String url, String dataList, @Nullable String firstEpisodeInSeason, @Nullable String numberOfEpisodesInSeason) {
		return url + "/" + dataList + "/" + (firstEpisodeInSeason != null ? firstEpisodeInSeason : (numberOfEpisodesInSeason != null ? episodeChecker(numberOfEpisodesInSeason) : null));
	}
	
	public String build(String url, @NotNull Map<String, String> queryParams) {
		return url + addQueryParametersToURL(queryParams);
	}
	
	private String addQueryParametersToURL(Map<String, String> queryParams) {
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
	
	private String episodeChecker(String episode) {
		Pattern pattern = Pattern.compile("(\\d{1,3}-(\\d{1,3}|[xX]{1,3}))");
		Matcher matcher = pattern.matcher(episode);
		if (matcher.find()) {
			String[] range = episode.split("-");
			return range[0];
		}
		return FIRST_EPISODE.getDescription();
	}
}
