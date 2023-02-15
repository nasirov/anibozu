package nasirov.yv.ab.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Nasirov Yuriy
 */
public class MalException extends RuntimeException {

	@Getter
	private final HttpStatus httpStatus;

	public MalException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}
}
