package nasirov.yv.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.enums.Constants;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class URLBuilderTest extends AbstractTest {

	private String url = animediaOnlineTv + "anime/smth";


	@Test
	public void build() throws Exception {
		String dataList = "1";
		String firstEpisodeInSeason = "1";
		String numberOfEpisodesInSeasonRange = "1-12";
		String numberOfEpisodesInSeasonNotNumber = "OVA";
		String resultUrl = URLBuilder.build(url, dataList, firstEpisodeInSeason, null);
		assertEquals(url + "/" + dataList + "/" + firstEpisodeInSeason, resultUrl);
		resultUrl = URLBuilder.build(url, dataList, null, numberOfEpisodesInSeasonRange);
		assertEquals(url + "/" + dataList + "/" + numberOfEpisodesInSeasonRange.split("-")[0], resultUrl);
		resultUrl = URLBuilder.build(url, dataList, null, numberOfEpisodesInSeasonNotNumber);
		assertEquals(url + "/" + dataList + "/" + Constants.FIRST_EPISODE.getDescription(), resultUrl);
	}
	@Test
	public void buildWithQueries() throws Exception {
		Map<String, String> queries = new LinkedHashMap<>();
		String firstParamKey = "firstParamKey";
		String firstParamValue = "firstParamValue";
		String secondParamKey = "secondParamKey";
		String secondParamValue = "secondParamValue";
		queries.put(firstParamKey, firstParamValue);
		queries.put(secondParamKey, secondParamValue);
		String resultUrl = URLBuilder.build(url, queries);
		assertEquals(url + "?" + firstParamKey + "=" + firstParamValue + "&" + secondParamKey + "=" + secondParamValue, resultUrl);
		queries.clear();
		resultUrl = URLBuilder.build(url, queries);
		assertEquals(url, resultUrl);
	}

	@Test
	public void testConstructor() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<?>[] declaredConstructors = URLBuilder.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		URLBuilder fromPrivateConstructor = (URLBuilder) declaredConstructors[0].newInstance();
		assertNotNull(fromPrivateConstructor);
	}

}