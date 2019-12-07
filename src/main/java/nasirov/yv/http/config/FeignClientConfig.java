package nasirov.yv.http.config;

import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import lombok.RequiredArgsConstructor;
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
		return new HeaderReplacerDecoder(new GZIPDecoder(new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters)))));
	}
}
