package app.diy.note_taking_app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.diy.note_taking_app.domain.dto.response.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.NoteRepository;
import app.diy.note_taking_app.repository.UserPermissionRepository;
import app.diy.note_taking_app.repository.UserRepository;
import app.diy.note_taking_app.service.factory.UserDetailFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final NoteRepository noteRepository;
	private final UserPermissionRepository userPermissionRepository;

	private final UserDetailFactory userDetailFactory;

	@Override
	public UserDetailResponse getUser(Integer userId) {
		return userDetailFactory
				.create(userRepository.findById(userId)
						.orElseThrow(() -> new UserNotFoundException("User was not found")));
	}

	@Override
	@Transactional
	public void delete(User user) {
		Integer userId = user.getId();
		List<Note> notes = noteRepository.findByCreatedUser_IdAndDeletedFlagFalse(userId);
		List<UserPermission> userPermissions = userPermissionRepository.findByUser_IdAndDeletedFlagFalse(userId);

		try {
			userRepository.deleteUser(userId);
			// delete linked notes and permissions if they existed
			if (!notes.isEmpty()) {
				noteRepository.deleteNotes(notes.stream().map(note -> note.getId()).toList(), user);
			}
			if (!userPermissions.isEmpty()) {
				userPermissionRepository.deleteUserPermissionsByIds(userPermissions.stream()
						.map(userPermission -> userPermission.getId())
						.toList());
			}
		} catch (Exception e) {
			throw new DatabaseTransactionalException("Failed to delete user", e);
		}
	}
}
