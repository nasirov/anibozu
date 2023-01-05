package nasirov.yv.ac.dto.cache;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class IntegerAsMapKeySerializer extends JsonSerializer<Integer> {

	@Override
	public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeFieldName(value.toString());
	}
}
