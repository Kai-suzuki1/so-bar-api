package app.diy.note_taking_app.exceptions;

public class InsufficientUserAuthorizationException extends RuntimeException {

	public InsufficientUserAuthorizationException(String message) {
		super(message);
	}

	public InsufficientUserAuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}
}
