package app.diy.note_taking_app.exceptions;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class DefaultExceptionHandler {

	/**
	 * Handling if validation error occurred
	 * {@link MethodArgumentNotValidException}
	 * and returns the cause of the validation error.
	 * {@link ApiValidationError}
	 * HttpStatus code is 400
	 * 
	 * @param e       if validation error occurred
	 * @param request request body
	 * @return {@code ResponseEntity<ApiValidationError>}
	 */
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

	/**
	 * Handling if a userName and password didn't match
	 * {@link BadCredentialsException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 400
	 * 
	 * @param e       if a userName and password didn't match
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleException(
			BadCredentialsException e,
			HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handling if a user's JWT token was already expired
	 * {@link ExpiredJwtException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 403
	 * 
	 * @param e       if a user's JWT token was already expired
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
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

	/**
	 * Handling if a certain user was not found
	 * {@link UserNotFoundException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 404
	 * 
	 * @param e       if a user is not found
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
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

	/**
	 * Handling if an information in JWT token did't match
	 * {@link MismatchUserDetailException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 405
	 * 
	 * @param e       if an information in JWT token did't match
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
	@ExceptionHandler(MismatchUserDetailException.class)
	public ResponseEntity<ApiError> handleException(
			MismatchUserDetailException e,
			HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.METHOD_NOT_ALLOWED.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
	}

	/**
	 * Handling if there is an error thrown during database process
	 * {@link SQLException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 500
	 * 
	 * @param e       if there is an error thrown during database process
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
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

	/**
	 * Handling if any unhandled exception occurred, basically an expected exception
	 * {@link Exception}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 500
	 * 
	 * @param e       if any unhandled exception occurred
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
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
