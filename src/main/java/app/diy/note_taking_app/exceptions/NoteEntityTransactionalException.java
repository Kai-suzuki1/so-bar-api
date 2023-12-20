package app.diy.note_taking_app.exceptions;

public class NoteEntityTransactionalException extends RuntimeException {

	public NoteEntityTransactionalException(String message) {
		super(message);
	}

	public NoteEntityTransactionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
