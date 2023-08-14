package app.diy.note_taking_app.exceptions;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class DefaultExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiError> handleException(
			UserNotFoundException e,
			HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.NOT_FOUND.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(SQLException.class)
	public ResponseEntity<ApiError> handleException(
			SQLException e,
			HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiValidationError> handleException(
			MethodArgumentNotValidException e,
			HttpServletRequest request) {

		ApiValidationError apiError = ApiValidationError.builder()
				.path(request.getRequestURI())
				.message(e.getFieldErrors()
						.stream()
						.map((fieldError) -> {
							return ValidationErrorMessage.builder()
									.fieldName(fieldError.getField())
									.detail(fieldError.getDefaultMessage())
									.build();
						}).toList())
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<ApiError> handleException(
			ExpiredJwtException e,
			HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.FORBIDDEN.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleException(
			Exception e,
			HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
