package nasirov.yv.parser;

/**
 * Created by nasirov.yv
 */
public interface AnidubParserI {

	String extractEpisodeNumber(String episodeName);

	String fixBrokenUrl(String url);
}
