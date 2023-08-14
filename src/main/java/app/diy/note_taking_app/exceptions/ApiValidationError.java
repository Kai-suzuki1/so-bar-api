package app.diy.note_taking_app.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiValidationError {

	private String path;
	private List<ValidationErrorMessage> message;
	private int statusCode;
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime localDateTime;
}
