package app.diy.note_taking_app.domain.dto;

import app.diy.note_taking_app.validation.removedPermissionUser.RemovedPermissionUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserAuthorization {

	@RemovedPermissionUser
	private Integer permissionId;
	private Integer userId;
	private PermissionType type;
}
