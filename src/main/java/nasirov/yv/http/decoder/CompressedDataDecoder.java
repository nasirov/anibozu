package nasirov.yv.http.decoder;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CONTENT_ENCODING;

import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Created by nasirov.yv
 */
public class CompressedDataDecoder implements Decoder {

	private final Decoder delegate;

	private Map<CompressionType, Decoder> compressedDataDecodeStrategies;

	public CompressedDataDecoder(Decoder delegate, Map<CompressionType, Decoder> compressedDataDecodeStrategies) {
		requireNonNull(delegate, "Decoder must not be null.");
		requireNonNull(compressedDataDecodeStrategies, "Decode Strategies must not be null.");
		this.delegate = delegate;
		this.compressedDataDecodeStrategies = compressedDataDecodeStrategies;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		String contentEncoding = extractContentEncoding(response.headers());
		return compressedDataDecodeStrategies.getOrDefault(CompressionType.getByName(contentEncoding), delegate)
				.decode(response, type);
	}

	private String extractContentEncoding(Map<String, Collection<String>> headers) {
		return headers.getOrDefault(CONTENT_ENCODING, emptyList())
				.stream()
				.findFirst()
				.orElse(null);
	}
}
