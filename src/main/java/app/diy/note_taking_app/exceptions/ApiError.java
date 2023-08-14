package app.diy.note_taking_app.exceptions;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime localDateTime;
}
