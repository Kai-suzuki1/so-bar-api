package app.diy.note_taking_app.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl implements UserPermissionService {

	private final UserPermissionRepository userPermissionRepository;

	@Override
	public boolean existsUndeletedPermissionById(Integer id) {
		return userPermissionRepository.existsByIdAndDeletedFlagFalse(id);
	}

	@Override
	public boolean canUpdateNote(Integer noteId, Integer userId) {
		Optional<UserPermission> permission = userPermissionRepository
				.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(noteId, userId);
		// if permission is not found, deleted, or not accepted yet
		if (permission.isEmpty()) {
			return false;
		}
		// if permission is invalid for updating note
		if (permission.get().toPermissionType().isReadOnly()) {
			return false;
		}
		return true;
	}
}
