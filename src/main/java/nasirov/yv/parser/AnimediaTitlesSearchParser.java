package nasirov.yv.parser;

import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Десериализирует JSON с информацией для поиска аниме на animedia в List<AnimediaTitleSearchInfo>
 * ajax/anime_list
 * Created by Хикка on 20.12.2018.
 */
@Component
public class AnimediaTitlesSearchParser {
	private final WrappedObjectMapper wrappedObjectMapper;
	
	@Autowired
	public AnimediaTitlesSearchParser(WrappedObjectMapper wrappedObjectMapper) {
		this.wrappedObjectMapper = wrappedObjectMapper;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(AnimediaTitlesSearchParser.class);
	
	/**
	 * Десериализирует JSON ответ от сайта
	 *
	 * @param response ответ от сайта
	 * @return список с информацией для поиска аниме
	 */
	@SuppressWarnings("unchecked")
	public <T extends Collection> T parse(HttpResponse response, Class<T> collection) {
		if (response == null) {
			logger.error("AnimediaResponse must be not null!");
			throw new RuntimeException("AnimediaResponse must be not null!");
		}
		logger.debug("Start Parsing");
		T titlesInfoForAnimediaSearch = wrappedObjectMapper.unmarshal(response.getContent(), AnimediaTitleSearchInfo.class, collection);
		logger.debug("End Parsing");
		return titlesInfoForAnimediaSearch;
	}
}
