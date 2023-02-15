package nasirov.yv.ab.controller;

import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.ab.dto.fe.ProcessResult;
import nasirov.yv.ab.exception.MalException;
import nasirov.yv.ab.service.MalAccessRestorerI;
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
@RequiredArgsConstructor
public class ExceptionHandlers {

	public static final String GENERIC_ERROR_MESSAGE = "Sorry, something went wrong. Please, try again later.";

	private static final ProcessResult FALLBACK = new ProcessResult(GENERIC_ERROR_MESSAGE);

	private final MalAccessRestorerI malAccessRestorer;

	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(MalException.class)
	public Mono<ProcessResult> handleMalException(MalException e) {
		String errorMessage = e.getMessage();
		log.error("{}", errorMessage);
		if (e.getHttpStatus() == HttpStatus.FORBIDDEN) {
			malAccessRestorer.restoreMalAccessAsync();
		}
		return Mono.just(new ProcessResult(errorMessage));
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public Mono<ProcessResult> handleValidationException(ConstraintViolationException e) {
		logException(e);
		return Mono.just(new ProcessResult(
				e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","))));
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public Mono<ProcessResult> handleGenericException(Exception e) {
		logException(e);
		return Mono.just(FALLBACK);
	}

	private void logException(Throwable e) {
		log.error("Exception has occurred", e);
	}
}
