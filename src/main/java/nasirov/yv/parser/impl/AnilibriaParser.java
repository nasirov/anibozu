package nasirov.yv.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.AnilibriaParserI;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class AnilibriaParser implements AnilibriaParserI {

	private static final Pattern PLAYER_EPISODES_PATTERN = Pattern.compile("'title':'Серия\\s+(?<episode>\\d{1,3})'");

	@Override
	public List<Integer> extractEpisodes(String titlePage) {
		List<Integer> result = new ArrayList<>();
		Matcher matcher = PLAYER_EPISODES_PATTERN.matcher(titlePage);
		while (matcher.find()) {
			result.add(Integer.valueOf(matcher.group("episode")));
		}
		return result;
	}
}
