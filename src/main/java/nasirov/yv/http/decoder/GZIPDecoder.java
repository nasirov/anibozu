package nasirov.yv.http.decoder;

import static feign.Util.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.zip.GZIPInputStream;

/**
 * Created by nasirov.yv
 */
public class GZIPDecoder implements Decoder {

	private final Decoder delegate;

	public GZIPDecoder(Decoder delegate) {
		requireNonNull(delegate, "Decoder must not be null. ");
		this.delegate = delegate;
	}

	@Override
	public Object decode(Response response, Type type) throws IOException {
		return delegate.decode(response.toBuilder()
				.status(response.status())
				.headers(response.headers())
				.body(new String(toByteArray(new GZIPInputStream(response.body()
						.asInputStream()))), UTF_8)
				.build(), type);
	}
}
