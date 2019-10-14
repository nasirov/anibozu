package nasirov.yv.exception.cloudflare;

/**
 * Created by nasirov.yv
 */
public class CookieNotFoundException extends RuntimeException {

	public CookieNotFoundException() {
	}
	public CookieNotFoundException(String message) {
		super(message);
	}
}
