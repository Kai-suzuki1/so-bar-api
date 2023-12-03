package app.diy.note_taking_app.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

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
	private LocalDateTime localDateTime;
}
