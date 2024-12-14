package nasirov.yv.anibozu.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import nasirov.yv.anibozu.properties.AppProps;
import nasirov.yv.anibozu.service.MalAnimeFormatterI;
import nasirov.yv.starter_common.dto.mal.MalAnime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class MalAnimeFormatter implements MalAnimeFormatterI {

	private static final Pattern RESOLUTION_PATTERN = Pattern.compile("(/r/\\d{1,3}x\\d{1,3})");

	private static final Pattern QUERY_PATTERN = Pattern.compile("(\\?s=.+)");

	private final AppProps appProps;

	@Override
	public MalAnime format(MalAnime anime) {
		String changedPosterUrl = StringUtils.EMPTY;
		Matcher matcher = RESOLUTION_PATTERN.matcher(anime.getPosterUrl());
		if (matcher.find()) {
			changedPosterUrl = matcher.replaceAll(StringUtils.EMPTY);
		}
		matcher = QUERY_PATTERN.matcher(changedPosterUrl);
		if (matcher.find()) {
			changedPosterUrl = matcher.replaceAll(StringUtils.EMPTY);
		}
		if (StringUtils.isNotBlank(changedPosterUrl)) {
			anime.setPosterUrl(changedPosterUrl);
		}
		anime.setUrl(appProps.getMal().getUrl() + anime.getUrl());
		anime.setName(HtmlUtils.htmlUnescape(anime.getName()));
		return anime;
	}
}
