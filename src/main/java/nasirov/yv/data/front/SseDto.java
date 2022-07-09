package nasirov.yv.data.front;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseDto {

	private EventType eventType;

	private TitleDto titleDto;

	private String errorMessage;
}
