package nasirov.yv.util;

import nasirov.yv.serialization.AnimediaMALTitleReferences;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nasirov.yv
 */
public class RoutinesParser {
	private static final String PARSE_URL = "http://online\\.animedia\\.tv/(?<path>(?<root>anime/.+?)/\\d{1,3}/\\d{1,3})";
	
	private static final String ORIGINAL_TITLE = "^Original title:\\s(?<originalTitle>[^а-яА-Я\\n]*\\w)";
	
	/**
	 * Парсит строку с урлами
	 *
	 * @param response строка с урлами
	 * @return список урлов
	 */
	public static List<String> getUrlList(String response) {
		if (response == null) {
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		return searchForUrls(response);
	}
	
	/**
	 * Парсит полный урл
	 *
	 * @param fullUrl full url
	 * @return url root
	 */
	public static String getUrlRoot(String fullUrl) {
		if (fullUrl == null) {
			throw new RuntimeException("Full Url must be not null!");
		}
		return searchForRootUrl(fullUrl);
	}
	
	public static List<AnimediaMALTitleReferences> getTitleReferences() {
		return null;
	}
	
	private static List<String> searchForUrls(String content) {
		Pattern pattern = Pattern.compile(PARSE_URL);
		Matcher matcher = pattern.matcher(content);
		List<String> urlList = new LinkedList<>();
		while (matcher.find()) {
			urlList.add(matcher.group("path"));
		}
		return urlList;
	}
	
	private static String searchForRootUrl(String content) {
		Pattern pattern = Pattern.compile(PARSE_URL);
		Matcher matcher = pattern.matcher(content);
		String urlList = null;
		while (matcher.find()) {
			urlList = matcher.group("root");
		}
		return urlList;
	}
}
