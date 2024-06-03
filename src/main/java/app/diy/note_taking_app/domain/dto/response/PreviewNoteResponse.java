package app.diy.note_taking_app.domain.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PreviewNoteResponse {

	private int id;
	private String title;
	private String contents;
	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime updatedAt;
	private String updatedBy;
	private boolean deletedFlag;
	private boolean deletableFlag;
}
