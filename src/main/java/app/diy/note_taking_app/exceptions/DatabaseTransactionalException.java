package app.diy.note_taking_app.exceptions;

public class DatabaseTransactionalException extends RuntimeException {

	public DatabaseTransactionalException(String message) {
		super(message);
	}

	public DatabaseTransactionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
