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
import java.util.regex.Pattern;
import nasirov.yv.data.anime_pik.api.AnimePikEpisode;
import nasirov.yv.data.animedia.site.SiteEpisode;
import org.springframework.http.MediaType;

/**
 * Created by nasirov.yv
 */
public class HeaderReplacerDecoder implements Decoder {

	/**
	 * Url part of Animedia data list info endpoint
	 * <p>
	 * Should be replaced with {@link MediaType#APPLICATION_JSON_VALUE} in order deserialize json to {@link SiteEpisode}
	 */
	private static final String ANIMEDIA_DATA_LIST_INFO_ENDPOINT = "/embeds/playlist-j.txt/";

	/**
	 * Pattern for AnimePik episodes deserialization
	 * <p>
	 * Should be replaced with {@link MediaType#APPLICATION_JSON_VALUE} in order deserialize json to {@link AnimePikEpisode}
	 */
	private static final Pattern ANIME_PIK_RESOURCES_ENDPOINT = Pattern.compile("/\\d+.txt$");

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
				.filter(x -> isContentTypeHeaderNeedsReplace(response))
				.findFirst()
				.ifPresent(x -> replaceContentTypeHeaderForJsonDeserialization(contentType));
		return delegate.decode(response.toBuilder()
				.headers(headers)
				.build(), type);
	}

	private boolean isContentTypeHeaderNeedsReplace(Response response) {
		String url = response.request()
				.url();
		return url.contains(ANIMEDIA_DATA_LIST_INFO_ENDPOINT) || ANIME_PIK_RESOURCES_ENDPOINT.matcher(url)
				.find();
	}

	private void replaceContentTypeHeaderForJsonDeserialization(Collection<String> contentTypeValues) {
		contentTypeValues.clear();
		contentTypeValues.add(APPLICATION_JSON_VALUE);
	}
}
