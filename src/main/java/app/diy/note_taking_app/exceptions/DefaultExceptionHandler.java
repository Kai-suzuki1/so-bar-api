package app.diy.note_taking_app.exceptions;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.diy.note_taking_app.domain.dto.ApiError;
import app.diy.note_taking_app.domain.dto.ApiValidationError;
import app.diy.note_taking_app.domain.dto.ValidationErrorMessage;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class DefaultExceptionHandler {

	/**
	 * Handling if AccessDeniedException & AuthenticationException error occurred
	 * {@link AuthenticationException}
	 * and returns the cause of the validation error.
	 * {@link ApiError}
	 * HttpStatus code is 403
	 * 
	 * @param e       if AccessDeniedException & AuthenticationException error
	 *                occurred
	 * @param request request body
	 * @return {@code ResponseEntity<ApiValidationError>}
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuthenticationException(Exception e, HttpServletRequest request) {

		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message("Failed to authenticate")
				.statusCode(HttpStatus.FORBIDDEN.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
	}

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
	 * Handling if a request is prohibited due to the lack of user authorization
	 * {@link InsufficientUserAuthorizationException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 403
	 * 
	 * @param e       if a request is prohibited due to the lack of user
	 *                authorization
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
	@ExceptionHandler(InsufficientUserAuthorizationException.class)
	public ResponseEntity<ApiError> handleException(
			InsufficientUserAuthorizationException e,
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
	 * Handling if a certain note was not found
	 * {@link NoteNotFoundException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 404
	 * 
	 * @param e       if a user is not found
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
	@ExceptionHandler(NoteNotFoundException.class)
	public ResponseEntity<ApiError> handleException(
			NoteNotFoundException e,
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
	 * Handling if there is an error thrown during database process of Note
	 * {@link NoteEntityTransactionalException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 500
	 * 
	 * @param e       if there is an error thrown during database process of Note
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
	@ExceptionHandler(NoteEntityTransactionalException.class)
	public ResponseEntity<ApiError> handleException(
			NoteEntityTransactionalException e,
			HttpServletRequest request) {
		e.printStackTrace();
		ApiError apiError = ApiError.builder()
				.path(request.getRequestURI())
				.message(e.getMessage())
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.localDateTime(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
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
	 * Handling if there is an error thrown during Json parse processing
	 * process
	 * {@link JsonConversionFailureException}
	 * and returns the detail of the exception.
	 * {@link ApiError}
	 * HttpStatus code is 500
	 * 
	 * @param e       if there is an error thrown during Json parse processing
	 * @param request request body
	 * @return {@code ResponseEntity<ApiError>}
	 */
	@ExceptionHandler(JsonConversionFailureException.class)
	public ResponseEntity<ApiError> handleException(
			JsonConversionFailureException e,
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
