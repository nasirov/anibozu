package nasirov.yv.http.config;

import static nasirov.yv.http.decoder.CompressionType.BROTLI;
import static nasirov.yv.http.decoder.CompressionType.GZIP;

import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nasirov.yv.http.decoder.BrotliDecoder;
import nasirov.yv.http.decoder.CompressedDataDecoder;
import nasirov.yv.http.decoder.CompressionType;
import nasirov.yv.http.decoder.GZIPDecoder;
import nasirov.yv.http.decoder.HeaderReplacerDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nasirov.yv
 */
@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

	private final ObjectFactory<HttpMessageConverters> messageConverters;

	@Bean
	public Decoder feignDecoder() {
		Decoder defaultDelegate = new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters)));
		return new HeaderReplacerDecoder(new CompressedDataDecoder(defaultDelegate, compressedDataDecodeStrategies(defaultDelegate)));
	}

	private Map<CompressionType, Decoder> compressedDataDecodeStrategies(Decoder defaultDelegate) {
		Map<CompressionType, Decoder> result = new EnumMap<>(CompressionType.class);
		result.put(BROTLI, new BrotliDecoder(defaultDelegate));
		result.put(GZIP, new GZIPDecoder(defaultDelegate));
		return result;
	}
}
