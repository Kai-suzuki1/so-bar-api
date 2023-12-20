package app.diy.note_taking_app.validation.removedPermissionUser;

import app.diy.note_taking_app.service.UserPermissionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RemovedPermissionUserValidator implements ConstraintValidator<RemovedPermissionUser, Integer> {

	private final UserPermissionService userPermissionService;

	@Override
	public boolean isValid(Integer permissionId, ConstraintValidatorContext context) {
		return userPermissionService.existsUndeletedPermissionById(permissionId);
	}
}
