package nasirov.yv.exception.mal;

/**
 * Created by nasirov.yv
 */
public class UnexpectedCallingException extends RuntimeException {

	public UnexpectedCallingException() {
	}
	public UnexpectedCallingException(String message) {
		super(message);
	}
	public UnexpectedCallingException(String message, Throwable cause) {
		super(message, cause);
	}
	public UnexpectedCallingException(Throwable cause) {
		super(cause);
	}
}
