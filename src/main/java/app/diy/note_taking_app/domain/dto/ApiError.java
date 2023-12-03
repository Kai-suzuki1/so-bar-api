package app.diy.note_taking_app.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiError {

	private String path;
	private String message;
	private int statusCode;
	private LocalDateTime localDateTime;
}
