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

	@Size(max = TITLE_MAX_SIZE)
	private String title;
	@Size(max = CONTENTS_MAX_SIZE)
	private String contents;
}
