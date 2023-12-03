package app.diy.note_taking_app.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDetailResponse {

	private int id;
	private String name;
	private String email;
	private String password;
}
