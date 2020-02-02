package nasirov.yv.http.decoder;

import static feign.Util.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import org.brotli.dec.BrotliInputStream;

/**
 * Created by nasirov.yv
 */
public class BrotliDecoder implements Decoder {

	private final Decoder delegate;

	public BrotliDecoder(Decoder delegate) {
		requireNonNull(delegate, "Decoder must not be null.");
		this.delegate = delegate;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		String decompressedBody = decompressBody(response);
		return delegate.decode(response.toBuilder()
				.status(response.status())
				.headers(response.headers())
				.body(decompressedBody, UTF_8)
				.build(), type);
	}

	private String decompressBody(Response response) throws IOException {
		return new String(toByteArray(new BrotliInputStream(response.body()
				.asInputStream())));
	}
}
