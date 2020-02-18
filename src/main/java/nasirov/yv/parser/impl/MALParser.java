package nasirov.yv.parser.impl;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.MALParserI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * MAL html parser
 * <p>
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class MALParser implements MALParserI {

	/**
	 * Searches for "Currently Watching" titles in an user profile html
	 *
	 * @param userProfile html with an user profile
	 * @return number of watching titles
	 */
	@Override
	public int getNumWatchingTitles(String userProfile) {
		return Integer.parseInt(extractNumberOfWatchingTitles(userProfile));
	}

	private String extractNumberOfWatchingTitles(String userProfile) {
		Document html = Jsoup.parse(userProfile);
		Elements spansAnimeAndMangaStats = html.select(".di-ib.fl-r.lh10");
		return spansAnimeAndMangaStats.stream()
				.filter(this::isTargetSpan)
				.map(Element::text)
				.findFirst()
				.orElse("0")
				.replace(",", "");
	}

	private boolean isTargetSpan(Element span) {
		return span.parent()
				.child(0)
				.hasClass("di-ib fl-l lh10 circle anime watching");
	}
}
