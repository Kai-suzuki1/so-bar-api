package app.diy.note_taking_app.exceptions;

public class JsonConversionFailureException extends RuntimeException {

	public JsonConversionFailureException(String message) {
		super(message);
	}

	public JsonConversionFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
