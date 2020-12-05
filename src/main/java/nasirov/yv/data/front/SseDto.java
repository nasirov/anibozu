package nasirov.yv.data.front;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto for Server-Sent Events
 * <p>
 * @author Nasirov Yuriy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
