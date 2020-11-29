package nasirov.yv.data.front;

/**
 * Enum for Server-Sent Events
 * <p>
 * @author Nasirov Yuriy
 */
public enum EventType {
	/**
	 * New episode is available
	 */
	AVAILABLE,
	/**
	 * New episode is not available
	 */
	NOT_AVAILABLE,
	/**
	 * Title is not found on fandub sites
	 */
	NOT_FOUND,
	/**
	 * Closing event
	 */
	DONE
}
