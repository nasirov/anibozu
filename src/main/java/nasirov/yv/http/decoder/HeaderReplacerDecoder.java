package nasirov.yv.http.decoder;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Created by nasirov.yv
 */
public class HeaderReplacerDecoder implements Decoder {

	private static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain;\\s?charset=.*";

	private static final String ANIMEDIA_DATA_LIST_INFO_ENDPOINT = "/embeds/playlist-j.txt/";

	private final Decoder delegate;

	public HeaderReplacerDecoder(Decoder delegate) {
		requireNonNull(delegate, "Decoder must not be null.");
		this.delegate = delegate;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		Map<String, Collection<String>> headers = response.headers();
		Collection<String> contentType = headers.getOrDefault(CONTENT_TYPE, emptyList());
		contentType.stream()
				.filter(x -> isContentTypeHeaderNeedsReplace(response, x))
				.findFirst()
				.ifPresent(x -> replaceContentTypeHeaderForJsonDeserialization(contentType, x));
		return delegate.decode(response.toBuilder()
				.headers(headers)
				.build(), type);
	}

	private boolean isContentTypeHeaderNeedsReplace(Response response, String contentTypeValue) {
		return contentTypeValue.matches(TEXT_PLAIN_CHARSET_UTF_8) || response.request()
				.url()
				.contains(ANIMEDIA_DATA_LIST_INFO_ENDPOINT);
	}

	private void replaceContentTypeHeaderForJsonDeserialization(Collection<String> contentTypeValues, String contentType) {
		contentTypeValues.removeIf(x -> x.matches(contentType));
		contentTypeValues.add(APPLICATION_JSON_VALUE);
	}
}
