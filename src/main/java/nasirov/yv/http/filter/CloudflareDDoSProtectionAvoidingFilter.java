package nasirov.yv.http.filter;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.exception.cloudflare.CookieNotFoundException;
import nasirov.yv.exception.cloudflare.SeedNotFoundException;
import nasirov.yv.util.URLBuilder;
import org.glassfish.jersey.message.internal.CookiesParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Component
public class CloudflareDDoSProtectionAvoidingFilter extends ClientFilter {

	private static final String DDOS_PROTECTION = "DDoS protection by Cloudflare";

	private static final String CF_COOKIE = "__cfduid";

	private static final String CF_CLEARANCE = "cf_clearance";

	private static final String CHALLENGE_FORM_REGEX = "<form id=\"challenge-form\" action=\"/(?<url>.+)\" method=\"get\">\\s*<input type=\"hidden\" "
			+ "name=\"(?<sName>.+)\"\\s*value=\"(?<sValue>.+)\"></input>\\s*<input type=\"hidden\" name=\"(?<jschlVcName>.+)\" value=\"(?<jschlVcValue>.+)"
			+ "\"/>\\s*<input type=\"hidden\" name=\"(?<passName>.+)\" value=\"(?<passValue>.+)\"/>\\s*<input type=\"hidden\" id=\"jschl-answer\" name=\""
			+ "(?<jschlAnswerName>.+)\"/>\\s*</form>";

	private static final String SEED_REGEX =
			"var[\\s\\w,]+\\R?\\s+(?<arrayName>.+)\\s?=\\s?\\{\"(?<stringSeed>.+)\":\\s?(?<numberSeedExpression>.*?)" + "};";

	private static final String ADDITIONAL_EXPRESSION_REGEX = "(?<operation>[+\\-*/]+)=(?<expressionWithNumberSeed>.*?);";

	private static final String HOST_REGEX = "(?<host>https?://.*?/).+";

	private static final Pattern SEED_REGEX_PATTERN = Pattern.compile(SEED_REGEX);

	private static final Pattern HOST_REGEX_PATTERN = Pattern.compile(HOST_REGEX);

	private static final Pattern FIRST_OPTION_OF_NUMBER_ONE_PATTERN = Pattern.compile("(!\\+\\[])");

	private static final Pattern SECOND_OPTION_OF_NUMBER_ONE_PATTERN = Pattern.compile("(!!\\[])");

	private static final Pattern FIRST_OPTION_OF_NUMBER_ZERO_PATTERN = Pattern.compile("(\\[])");

	private static final Pattern CHALLENGE_FORM_REGEX_PATTERN = Pattern.compile(CHALLENGE_FORM_REGEX);

	@Override
	public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
		ClientHandler clientHandler = getNext();
		ClientResponse clientResponse = clientHandler.handle(clientRequest);
		if (clientResponse.getStatus() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
			String content = clientResponse.getEntity(String.class);
			if (content.contains(DDOS_PROTECTION)) {
				String url = clientRequest.getURI().toString();
				String verificationUrl = getVerificationUrl(content, url);
				setNewURI(clientRequest, verificationUrl);
				setCookie(clientResponse, clientRequest, CF_COOKIE);
				setReferer(clientRequest, url);
				clientResponse = clientHandler.handle(clientRequest);
				if (clientResponse.getStatus() == HttpStatus.FOUND.value()) {
					String location = clientResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
					setNewURI(clientRequest, location);
					setCookie(clientResponse, clientRequest, CF_CLEARANCE);
					clientResponse = clientHandler.handle(clientRequest);
				}
			} else {
				clientResponse.setEntityInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			}
		}
		return clientResponse;
	}

	@SneakyThrows
	private void setNewURI(ClientRequest clientRequest, String url) {
		clientRequest.setURI(new URI(url));
	}

	private void setCookie(ClientResponse response, ClientRequest request, String cookieName) {
		MultivaluedMap<String, String> headers = response.getHeaders();
		String cookieHeader = headers.get(HttpHeaders.SET_COOKIE).stream().filter(cookie -> cookie.contains(cookieName)).findFirst()
				.orElseThrow(() -> new CookieNotFoundException((cookieName + " not available!")));
		Cookie cookie = CookiesParser.parseCookie(cookieHeader);
		String oldCookie = (String) request.getHeaders().getFirst(HttpHeaders.COOKIE);
		String newCookie = cookie.getName() + "=" + cookie.getValue();
		String finalCookie = oldCookie != null ? oldCookie + "; " + newCookie : newCookie;
		request.getHeaders().putSingle(HttpHeaders.COOKIE, finalCookie);
	}

	private void setReferer(ClientRequest request, String url) {
		MultivaluedMap<String, Object> headers = request.getHeaders();
		headers.add(HttpHeaders.REFERER, url);
	}

	private String getVerificationUrl(String content, String url) {
		String finalUrl;
		Matcher matcher = SEED_REGEX_PATTERN.matcher(content);
		if (matcher.find()) {
			String arrayName = matcher.group("arrayName");
			String stringSeed = matcher.group("stringSeed");
			String numberSeedExpression = matcher.group("numberSeedExpression");
			Double seedSumDouble = Double.parseDouble(getSum(numberSeedExpression));
			Pattern additionalExpressionRegexPattern = Pattern.compile(arrayName + "\\." + stringSeed + ADDITIONAL_EXPRESSION_REGEX);
			matcher = additionalExpressionRegexPattern.matcher(content);
			while (matcher.find()) {
				String operation = matcher.group("operation");
				String expressionWithNumberSeed = matcher.group("expressionWithNumberSeed");
				switch (operation) {
					case "+":
						seedSumDouble += Double.parseDouble(getSum(expressionWithNumberSeed));
						break;
					case "-":
						seedSumDouble -= Double.parseDouble(getSum(expressionWithNumberSeed));
						break;
					case "*":
						seedSumDouble *= Double.parseDouble(getSum(expressionWithNumberSeed));
						break;
					default:
						break;
				}
			}
			String host = "";
			matcher = HOST_REGEX_PATTERN.matcher(url);
			if (matcher.find()) {
				host = matcher.group("host");
			}
			url = url.replaceAll("https?://", "");
			url = url.substring(0, url.length() - 1);
			NumberFormat numberInstance = NumberFormat.getIntegerInstance();
			numberInstance.setMaximumFractionDigits(10);
			seedSumDouble = Double.parseDouble(numberInstance.format(seedSumDouble).replace(",", ".")) + url.length();
			Map<String, String> queriesForUrl = getQueriesForUrl(content);
			queriesForUrl.put("jschl_answer", String.valueOf(seedSumDouble));
			String urlPath = queriesForUrl.get("url");
			queriesForUrl.remove("url");
			finalUrl = URLBuilder.build(host + urlPath, queriesForUrl);
		} else {
			throw new SeedNotFoundException(
					"Cloudflare DDoS Protection Seed Is Not Found! Check response and Regex!\nResponse from " + url + " :\n" + content);
		}
		return finalUrl;
	}

	private String getSum(String expression) {
		String unobfuscatedExpression = replaceObfuscatedValuesWithNumbers(expression);
		String expressionsSummaryWithDivisionOperation = getExpressionsSummaryWithDivisionOperation(unobfuscatedExpression);
		return divideExpressions(expressionsSummaryWithDivisionOperation);
	}

	private String replaceObfuscatedValuesWithNumbers(String expression) {
		String result = "";
		Matcher matcher = FIRST_OPTION_OF_NUMBER_ONE_PATTERN.matcher(expression);
		if (matcher.find()) {
			result = matcher.replaceAll("1");
		}
		matcher = SECOND_OPTION_OF_NUMBER_ONE_PATTERN.matcher(result);
		if (matcher.find()) {
			result = matcher.replaceAll("1");
		}
		matcher = FIRST_OPTION_OF_NUMBER_ZERO_PATTERN.matcher(result);
		if (matcher.find()) {
			result = matcher.replaceAll("0");
		}
		return result;
	}

	private String getExpressionsSummaryWithDivisionOperation(String unobfuscatedExpression) {
		String[] splittedByDivisionOperation = unobfuscatedExpression.split("/");
		StringBuilder expressionsSummaryWithDivisionOperation = new StringBuilder();
		for (String expressionWithAddition : splittedByDivisionOperation) {
			StringBuilder numbersOfExpressionsSummary = new StringBuilder();
			String[] isolatedExpressionsInBrackets = expressionWithAddition.split("\\)");
			for (String expression : isolatedExpressionsInBrackets) {
				char[] expressionChars = expression.toCharArray();
				int expressionSum = 0;
				for (char isolatedChar : expressionChars) {
					if (isolatedChar == '0' || isolatedChar == '1') {
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
		double finalSummary = 0;
		for (int i = 0; i < expressionsSummary.length; i++) {
			if (i == 0) {
				finalSummary = Integer.parseInt(expressionsSummary[i]);
			} else {
				finalSummary /= Integer.parseInt(expressionsSummary[i]);
			}
		}
		return String.valueOf(finalSummary);
	}

	private Map<String, String> getQueriesForUrl(String content) {
		Map<String, String> queries = new LinkedHashMap<>();
		Matcher matcher = CHALLENGE_FORM_REGEX_PATTERN.matcher(content);
		if (matcher.find()) {
			queries.put("url", matcher.group("url"));
			queries.put("s", matcher.group("sValue"));
			queries.put("jschl_vc", matcher.group("jschlVcValue"));
			queries.put("pass", matcher.group("passValue"));
			queries.put("jschl_answer", matcher.group("jschlAnswerName"));
		}
		return queries;
	}
}