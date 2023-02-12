package nasirov.yv.ab.controller;

import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.ProcessResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

	public static final String GENERIC_ERROR_MESSAGE = "Sorry, something went wrong. Please, try again later.";

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public Mono<ProcessResult> handleValidationException(ConstraintViolationException e) {
		logError(e);
		return Mono.just(new ProcessResult(
				e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","))));
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public Mono<ProcessResult> handleGenericException(Exception e) {
		logError(e);
		return Mono.just(new ProcessResult(GENERIC_ERROR_MESSAGE));
	}

	private void logError(Exception e) {
		log.error("Exception has occurred", e);
	}
}
