package nasirov.yv.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import nasirov.yv.data.constants.BaseConstants;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class URLBuilderTest {

	private String url = "https://online.animedia.tv/" + "anime/smth";


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
		assertEquals(url + "/" + dataList + "/" + BaseConstants.FIRST_EPISODE, resultUrl);
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
	public void testForbiddenPrivateConstructor() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<?>[] declaredConstructors = URLBuilder.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		try {
			declaredConstructors[0].newInstance();
		} catch (InvocationTargetException e) {
			assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
		}
	}

}