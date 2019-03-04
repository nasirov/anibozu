package nasirov.yv;

import nasirov.yv.configuration.AppConfiguration;
import nasirov.yv.http.HttpCaller;
import nasirov.yv.http.HttpCallerImpl;
import nasirov.yv.parameter.AnimediaRequestParametersBuilder;
import nasirov.yv.parameter.RequestParametersBuilder;
import nasirov.yv.parser.AnimediaHTMLParser;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.repository.NotFoundAnimeOnAnimediaRepository;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import nasirov.yv.serialization.AnimediaTitleSearchInfo;
import nasirov.yv.serialization.UserMALTitleInfo;
import nasirov.yv.service.ReferencesManager;
import nasirov.yv.service.SeasonAndEpisodeChecker;
import nasirov.yv.util.RoutinesIO;
import nasirov.yv.util.URLBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * Created by nasirov.yv
 */
@SpringBootTest(classes = {
		WrappedObjectMapper.class,
		CacheManager.class,
		AppConfiguration.class,
		URLBuilder.class,
		RoutinesIO.class,
		AnimediaHTMLParser.class,
		AnimediaRequestParametersBuilder.class,
		ReferencesManager.class,
		SeasonAndEpisodeChecker.class,
		HttpCallerImpl.class})

public class Caller extends AbstractTest {
	@Autowired
	private ReferencesManager referencesManager;
	
	@Autowired
	private SeasonAndEpisodeChecker seasonAndEpisodeChecker;
	
	@Autowired
	private RoutinesIO routinesIO;
	
	@Autowired
	private HttpCaller httpCaller;
	
	@Autowired
	@Qualifier("animediaRequestParametersBuilder")
	private RequestParametersBuilder requestParametersBuilder;
	
	@MockBean
	private NotFoundAnimeOnAnimediaRepository notFoundAnimeOnAnimediaRepository;
	
	private List<UserMALTitleInfo> notFoundOnAnimediaRepoMock;
	
	@Before
	public void setUp() {
		notFoundOnAnimediaRepoMock = new ArrayList<>();
		doAnswer(answer -> {
			notFoundOnAnimediaRepoMock.add(answer.getArgument(0));
			return (answer.getArgument(0));
		}).when(notFoundAnimeOnAnimediaRepository).saveAndFlush(any(UserMALTitleInfo.class));
		doAnswer(answer -> notFoundOnAnimediaRepoMock.stream().filter(list -> String.valueOf(list.getTitle()).equals(answer.getArgument(0))).count() > 0).when(notFoundAnimeOnAnimediaRepository).exitsByTitle(anyString());
	}
	
	@Test
	public void testSmth() {
		System.out.println("=======================================================================");
		System.out.println("START");
		System.out.println("Phase 1 start");
		System.out.println("=======================================================================");
		Set<UserMALTitleInfo> watchingTitles = routinesIO.unmarshalFromFile("aLotOfTitles.json", UserMALTitleInfo.class, LinkedHashSet.class);
		System.out.println("Phase 1 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 2 start");
		System.out.println("=======================================================================");
		Set<AnimediaMALTitleReferences> allReferences = referencesManager.getMultiSeasonsReferences();
		System.out.println("Phase 2 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 3 start");
		System.out.println("=======================================================================");
		referencesManager.updateReferences(allReferences);
		System.out.println("Phase 3 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 4 start");
		System.out.println("=======================================================================");
		routinesIO.marshalToFile("updatedAllReferences.json", allReferences);
		System.out.println("Phase 4 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 5 start");
		System.out.println("=======================================================================");
		Set<AnimediaTitleSearchInfo> animediaSearchList = routinesIO.unmarshalFromFile("search.json", AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		System.out.println("Phase 5 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 6 start");
		System.out.println("=======================================================================");
		Set<AnimediaMALTitleReferences> matchedReferences = referencesManager.getMatchedReferences(allReferences, watchingTitles);
		System.out.println("Phase 6 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 7 start");
		System.out.println("=======================================================================");
		Set<AnimediaMALTitleReferences> matchedAnime =seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, animediaSearchList, "test");
		System.out.println("Phase 7 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 8 start");
		System.out.println("=======================================================================");
		routinesIO.marshalToFile("matchedAnime.json", matchedAnime);
		System.out.println("Phase 8 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 9 start");
		System.out.println("=======================================================================");
		routinesIO.marshalToFile("notFoundOnAnimedia.json", notFoundOnAnimediaRepoMock);
		System.out.println("Phase 9 start");
		System.out.println("=======================================================================");
		System.out.println("END");
		System.out.println("=======================================================================");
	}
	
	@Test
	public void testSmth2() {
		System.out.println("=======================================================================");
		System.out.println("START");
		System.out.println("Phase 1 start");
		System.out.println("=======================================================================");
		Set<UserMALTitleInfo> watchingTitles = routinesIO.unmarshalFromFile("aLotOfTitles.json", UserMALTitleInfo.class, LinkedHashSet.class);
		System.out.println("Phase 1 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 2 start");
		System.out.println("=======================================================================");
		Set<AnimediaMALTitleReferences> allReferences = routinesIO.unmarshalFromFile("updatedAllReferences.json", AnimediaMALTitleReferences.class, LinkedHashSet.class);
		System.out.println("Phase 2 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 3 start");
		System.out.println("=======================================================================");
		Set<AnimediaTitleSearchInfo> animediaSearchList = routinesIO.unmarshalFromFile("search.json", AnimediaTitleSearchInfo.class, LinkedHashSet.class);
		System.out.println("Phase 3 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 4 start");
		System.out.println("=======================================================================");
		Set<AnimediaMALTitleReferences> matchedReferences = referencesManager.getMatchedReferences(allReferences, watchingTitles);
		System.out.println("Phase 4 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 5 start");
		System.out.println("=======================================================================");
		Set<AnimediaMALTitleReferences> matchedAnime =seasonAndEpisodeChecker.getMatchedAnime(watchingTitles, matchedReferences, animediaSearchList, "test");
		System.out.println("Phase 5 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 6 start");
		System.out.println("=======================================================================");
		routinesIO.marshalToFile("matchedAnime.json", matchedAnime);
		System.out.println("Phase 6 end");
		System.out.println("=======================================================================");
		System.out.println("Phase 7 start");
		System.out.println("=======================================================================");
		routinesIO.marshalToFile("notFoundOnAnimedia.json", notFoundOnAnimediaRepoMock);
		System.out.println("Phase 7 start");
		System.out.println("=======================================================================");
		System.out.println("END");
		System.out.println("=======================================================================");
	}
	
	@Test
	public void asd() {
		String expression = "+((!+[] + !![] + !![] + !![] + !![] + !![] + !![] + !![] + !![] + []) + (!+[] + !![]) + (+[]) + (!+[] + !![] + !![] + !![]) + (+!![]) + (!+[] + !![] + !![] + !![] + !![] + !![] + !![] + !![] + !![]) + (!+[] + !![] + !![] + !![] + !![] + !![] + !![] + !![]) + (!+[] + !![]) + (!+[] + !![] + !![])) / +((!+[] + !![] + !![] + !![] + !![] + !![] + !![] + !![] + []) + (!+[] + !![] + !![] + !![] + !![] + !![] + !![] + !![] + !![]) + (!+[] + !![] + !![] + !![]) + (!+[] + !![] + !![]) + (!+[] + !![] + !![] + !![] + !![] + !![]) + (+!![]) + (!+[] + !![] + !![] + !![] + !![] + !![] + !![] + !![]) + (!+[] + !![] + !![]) + (+!![]))";
		String unobfuscatedExpression = replaceObfuscatedValuesWithNumbers(expression);
		String expressionsSummaryWithDivisionOperation = getExpressionsSummaryWithDivisionOperation(unobfuscatedExpression);
		String fixedNumber = divideExpressions(expressionsSummaryWithDivisionOperation);
		System.out.println(fixedNumber);
	}
	
	private String replaceObfuscatedValuesWithNumbers(String expression) {
		String result = "";
		Pattern pattern = Pattern.compile("(!\\+\\[])");
		Matcher matcher = pattern.matcher(expression);
		if (matcher.find()) {
			result = matcher.replaceAll("1");
		}
		pattern = Pattern.compile("(!!\\[])");
		matcher = pattern.matcher(result);
		if (matcher.find()) {
			result = matcher.replaceAll("1");
		}
		pattern = Pattern.compile("(\\[])");
		matcher = pattern.matcher(result);
		if (matcher.find()) {
			result = matcher.replaceAll("0");
		}
		return result;
	}
	
	private String getExpressionsSummaryWithDivisionOperation(String unobfuscatedExpression) {
		String[] splitedByDivisionOperation = unobfuscatedExpression.split("\\s/\\s");
		//Arrays.stream(splitedByDivisionOperation).forEach(System.out::println);
		StringBuilder expressionsSummaryWithDivisionOperation = new StringBuilder();
		for (String expressionWithAddition : splitedByDivisionOperation) {
			StringBuilder numbersOfExpressionsSummary = new StringBuilder();
			String[] isolatedExpressionsInBrackets = expressionWithAddition.split("\\)");
			for (String expression : isolatedExpressionsInBrackets) {
				char[] expressionChars = expression.toCharArray();
				int expressionSum = 0;
				for (char isolatedChar : expressionChars) {
					if(isolatedChar == '0' || isolatedChar == '1'){
						expressionSum += Integer.parseInt(String.valueOf(isolatedChar));
					}
				}
				numbersOfExpressionsSummary.append(expressionSum);
			}
			expressionsSummaryWithDivisionOperation.append(numbersOfExpressionsSummary).append("/");
		}
		return expressionsSummaryWithDivisionOperation.toString();
	}
	
	private String divideExpressions(String expressionsSummaryWithDivisionOperation) {
		String[] expressionsSummary = expressionsSummaryWithDivisionOperation.split("/");
		double finalSummary=0;
		for(int i=0; i< expressionsSummary.length; i ++) {
			if (i == 0) {
				finalSummary = Integer.parseInt(expressionsSummary[i]);
			} else  {
				finalSummary /= Integer.parseInt(expressionsSummary[i]);
			}
		}
		return String.valueOf(finalSummary).substring(0, 12);
	}

//	@Test
//	public void test1() {
//		String url = "http://online.animedia.tv/cdn-cgi/l/chk_jschl?s=6c663f9a9a33e922b3c3249cbf2962cbf442df04-1551510849-1800-AZ%2BOsKFTHKQI35SoMarv0k2wX2WqaXa7rn%2FiWLnuwS8MOjrVrsWCypEQ5mxGgDnypAZOGv%2BEgglb2o9qjg7%2FixMVjlh%2FrVUSAsrZFXMFo8tF&jschl_vc=a73b822abb1e354394beee23a16f3399&pass=1551510853.75-m1Qe4Q2w%2F1&jschl_answer=21.3092219894";
//		httpCaller.call(url, HttpMethod.GET, requestParametersBuilder.build());
//	}

//	@Test
//	public void z() {
//		String posterLowQualityQueryParameters = "h=70&q=50";
//		String posterHighQualityQueryParameters = "h=350&q=100";
//		String secureProtocol = "https:";
//		Set<AnimediaTitleSearchInfo> linkedHashSet = routinesIO.unmarshalFromFile("2.json", AnimediaTitleSearchInfo.class, LinkedHashSet.class);
//		Set<AnimediaTitleSearchInfo> linkedHashSet2 = routinesIO.unmarshalFromFile("search.json", AnimediaTitleSearchInfo.class, LinkedHashSet.class);
//		linkedHashSet.forEach(set -> {
//			set.setUrl(set.getUrl().replaceAll(animediaOnlineTv, "")
//					.replace("[", "%5B").replace("]", "%5D"));
//			set.setPosterUrl(secureProtocol + set.getPosterUrl().replace(posterLowQualityQueryParameters, posterHighQualityQueryParameters));});
//		for (AnimediaTitleSearchInfo animediaTitleSearchInfo : linkedHashSet) {
//			linkedHashSet2.stream().filter(set -> set.getTitle().equals(animediaTitleSearchInfo.getTitle())).forEach(set -> {
//				set.setUrl(animediaTitleSearchInfo.getUrl());
//				set.setPosterUrl(animediaTitleSearchInfo.getPosterUrl());
//			});
//		}
//		routinesIO.marshalToFile("search.json", linkedHashSet2);
	//}
}
