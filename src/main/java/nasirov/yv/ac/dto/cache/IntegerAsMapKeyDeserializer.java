package nasirov.yv.ac.dto.cache;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class IntegerAsMapKeyDeserializer extends KeyDeserializer {

	@Override
	public Integer deserializeKey(String key, DeserializationContext ctxt) {
		return Integer.valueOf(key);
	}
}
