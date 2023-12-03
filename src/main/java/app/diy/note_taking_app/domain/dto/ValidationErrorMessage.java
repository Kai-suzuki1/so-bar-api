package app.diy.note_taking_app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ValidationErrorMessage {

	String fieldName;
	String detail;
}
