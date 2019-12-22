package nasirov.yv.parser;

public interface AnimediaHTMLParserI {

	String extractEpisodeNumber(String episodeName);

	String getAnimeId(String content);
}
