package app.diy.note_taking_app.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.diy.note_taking_app.exceptions.JsonConversionFailureException;

public final class StringUtil {

	// to prevent from being instantiated
	private StringUtil() {
	}

	public static String convertJsonToString(Object value, ObjectMapper objectMapper)
			throws JsonConversionFailureException {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new JsonConversionFailureException(e.getMessage(), e);
		}
	}

	public static String readFile(String filename) throws IOException {
		Path resourcesPath = Paths.get("src/test/java/app/diy/note_taking_app/resources");

		return Files.readString(Files.walk(resourcesPath)
				// filter only files
				.filter(Files::isRegularFile)
				// match only if the filename is the same name of the file
				.filter(path -> Objects.nonNull(path.getFileName()) && path.getFileName().toString().equals(filename))
				.findFirst()
				.orElseThrow(() -> new IOException()));
	}
}
