package app.diy.note_taking_app.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteUpdateRequest {

	private static final int TITLE_MAX_SIZE = 255;
	private static final int CONTENTS_MAX_SIZE = 65535;

	@Size(max = TITLE_MAX_SIZE, message = "Title should be {max} words or less")
	private String title;
	@Size(max = CONTENTS_MAX_SIZE, message = "Contents should be {max} words or less")
	private String contents;
}
