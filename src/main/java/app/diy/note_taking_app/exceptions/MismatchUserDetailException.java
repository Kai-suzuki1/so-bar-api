package app.diy.note_taking_app.exceptions;

public class MismatchUserDetailException extends RuntimeException {

	public MismatchUserDetailException(String message) {
		super(message);
	}

	public MismatchUserDetailException(String message, Throwable cause) {
		super(message, cause);
	}
}
