package nasirov.yv.parser;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Десериализирует JSON с информацией для поиска аниме на animedia в List<AnimediaTitleSearchInfo>
 * ajax/anime_list
 * Created by Хикка on 20.12.2018.
 */
@Component
@Slf4j
public class AnimediaTitlesSearchParser {
	private final WrappedObjectMapper wrappedObjectMapper;
	
	@Autowired
	public AnimediaTitlesSearchParser(WrappedObjectMapper wrappedObjectMapper) {
		this.wrappedObjectMapper = wrappedObjectMapper;
	}
	
	/**
	 * Десериализирует JSON ответ от сайта
	 *
	 * @param response ответ от сайта
	 * @return список с информацией для поиска аниме
	 */
	@SuppressWarnings("unchecked")
	public <T extends Collection> T parse(HttpResponse response, Class<T> collection) {
		if (response == null) {
			throw new NullPointerException("HttpResponse is null!");
		}
		return wrappedObjectMapper.unmarshal(response.getContent(), AnimediaTitleSearchInfo.class, collection);
	}
}
