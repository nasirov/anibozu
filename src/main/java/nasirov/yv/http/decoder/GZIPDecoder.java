package nasirov.yv.http.decoder;

import static feign.Util.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CONTENT_ENCODING;

import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by nasirov.yv
 */
public class GZIPDecoder implements Decoder {

	private static final String GZIP = "gzip";

	private final Decoder delegate;

	public GZIPDecoder(Decoder delegate) {
		requireNonNull(delegate, "Decoder must not be null. ");
		this.delegate = delegate;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		Map<String, Collection<String>> headers = response.headers();
		Collection<String> orDefault = headers.getOrDefault(CONTENT_ENCODING, emptyList());
		if (orDefault.contains(GZIP)) {
			return delegate.decode(response.toBuilder()
					.status(response.status())
					.headers(headers)
					.body(new String(toByteArray(new GZIPInputStream(response.body()
							.asInputStream()))), UTF_8)
					.build(), type);
		}
		return delegate.decode(response, type);
	}
}
