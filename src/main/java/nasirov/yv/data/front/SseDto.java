package nasirov.yv.data.front;

import lombok.Builder;
import lombok.Data;

/**
 * Dto for Server-Sent Events
 * <p>
 * Created by nasirov.yv
 */
@Data
@Builder
public class SseDto {

	/**
	 * Episode availability
	 */
	private EventType eventType;

	/**
	 * Dto with a title info
	 */
	private Anime anime;
}
