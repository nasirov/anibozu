package nasirov.yv.parser;

/**
 * Created by nasirov.yv
 */
public interface AnidubParserI {

	Integer extractEpisodeNumber(String episodeName);

	String fixBrokenUrl(String url);
}
