package nasirov.yv.exception.cloudflare;

/**
 * Created by nasirov.yv
 */
public class SeedNotFoundException extends RuntimeException {

	public SeedNotFoundException() {
	}
	public SeedNotFoundException(String message) {
		super(message);
	}
}
