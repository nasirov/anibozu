package nasirov.yv.http.decoder;

import static java.util.Arrays.stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public enum CompressionType {
	BROTLI("br"), GZIP("gzip");

	@Getter
	private final String name;

	public static CompressionType getByName(String name) {
		return stream(values()).filter(x -> x.getName()
				.equals(name))
				.findFirst()
				.orElse(null);
	}
}
