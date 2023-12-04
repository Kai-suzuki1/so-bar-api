package app.diy.note_taking_app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserAuthorization {

	private Integer permissionId;
	private Integer userId;
	private PermissionType type;
}
