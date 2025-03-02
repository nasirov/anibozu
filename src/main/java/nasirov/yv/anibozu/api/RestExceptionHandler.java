package nasirov.yv.anibozu.api;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.anibozu.dto.api.ApiErrorResponse;
import nasirov.yv.anibozu.exception.MalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

	private static final Mono<ApiErrorResponse> GENERIC_FALLBACK = Mono.just(
			new ApiErrorResponse("Sorry, something went wrong. Please try again later."));

	private static final Mono<ApiErrorResponse> VALIDATION_FALLBACK = Mono.just(new ApiErrorResponse("Invalid request."));

	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(MalException.class)
	public Mono<ApiErrorResponse> handleMalException(MalException e) {
		String errorMessage = e.getMessage();
		log.error("{}", errorMessage);
		return Mono.just(new ApiErrorResponse(errorMessage));
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {ConstraintViolationException.class, WebExchangeBindException.class, ServerWebInputException.class})
	public Mono<ApiErrorResponse> handleValidationException(Exception e) {
		logException(e);
		return VALIDATION_FALLBACK;
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public Mono<ApiErrorResponse> handleGenericException(Exception e) {
		logException(e);
		return GENERIC_FALLBACK;
	}

	private void logException(Exception e) {
		log.error("Exception has occurred", e);
	}
}
