package com.softKit.softKit_BE.shared.exception;

import com.softKit.softKit_BE.shared.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ---------- @RequestBody (@Valid) validation ----------

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseWithBindException(exception, request, "Validation error");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// ---------- QUERY PARAM / PATH VARIABLE validation ----------

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(
			BindException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseWithBindException(exception, request, "Validation error");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// ---------- DOMAIN: USER / EMAIL ----------

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(
			UserNotFoundException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseBody(exception, request, "User not found", "id");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(EmailAlreadyInUseException.class)
	public ResponseEntity<ErrorResponse> handleEmailAlreadyInUse(
			EmailAlreadyInUseException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseBody(exception, request, "Email already in use", "email");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	// ---------- AUTHENTICATION / CREDENTIALS / TOKEN ----------

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseBody(exception, request, "Invalid credentials", "credentials");

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUsernameNotFound(
			UsernameNotFoundException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseBody(exception, request, "User not found", "username");

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(
			InvalidTokenException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseBody(exception, request, "Invalid or expired token", "token");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// ---------- GENERIC DOMAIN RULES ----------

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
			IllegalArgumentException exception,
			HttpServletRequest request
	) {

		ErrorResponse body = buildErrorResponseBody(exception, request, "Invalid request", "error");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// ---------- CATCH-ALL ----------

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(
			Exception exception,
			HttpServletRequest request
	) {
		// TODO: to log 'exception' in a logger/observability

		ErrorResponse body = buildErrorResponseBody(exception, request, "Internal server error", "error");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	public ErrorResponse buildErrorResponseBody(Exception exception, HttpServletRequest request, String errorMessage, String k1) {

		Map<String, String> errors = Map.of(
				k1, exception.getMessage()
		);

		return new ErrorResponse(
				errorMessage,
				errors,
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				request.getRequestURI(),
				Instant.now()
		);

	}

	public ErrorResponse buildErrorResponseWithBindException(BindException exception, HttpServletRequest request, String errorMessage) {
		Map<String, String> fieldErrors = new HashMap<>();

		exception.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = error instanceof FieldError fieldError
					? fieldError.getField()
					: error.getObjectName();
			String bindErrorMessage = error.getDefaultMessage();
			fieldErrors.put(fieldName, bindErrorMessage);
		});

		return new ErrorResponse(
				errorMessage,
				fieldErrors,
				HttpStatus.BAD_REQUEST.value(),
				request.getRequestURI(),
				Instant.now()
		);
	}
}