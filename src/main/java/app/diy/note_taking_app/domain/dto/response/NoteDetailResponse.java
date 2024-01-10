package app.diy.note_taking_app.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import app.diy.note_taking_app.domain.dto.UserAuthorization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class NoteDetailResponse {

	private int id;
	private String title;
	private String contents;
	private boolean userIsAuthor;
	private List<UserAuthorization> sharedUsers;
	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime updatedAt;
	private String updatedBy;
}
