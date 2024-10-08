package app.diy.note_taking_app.service.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoteFactory {

	private final UserPermissionRepository userPermissionRepository;

	public List<PreviewNoteResponse> createPreviewNoteResponseList(List<Note> notes, Integer userId) {
		return notes.stream()
				.map(note -> PreviewNoteResponse.builder()
						.id(note.getId())
						.title(note.getTitle())
						.contents(note.getContents())
						.createdAt(note.getCreatedAt())
						.createdBy(note.getCreatedUser().getName())
						.updatedAt(note.getUpdatedAt())
						.updatedBy(note.getUpdatedUser().getName())
						.deletedFlag(note.isDeletedFlag())
						// if note is not deleted and
						// log-in user is author or shared user with read-write permission, return true
						.deletableFlag(
								!note.isDeletedFlag()
										&& (note.getCreatedUser().getId() == userId
												|| userPermissionRepository
														.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(note.getId(), userId)
														.filter(userPermission -> userPermission.toPermissionType().isReadWrite())
														.isPresent()))
						.build())
				.toList();
	}

	public NoteDetailResponse createNoteDetailResponse(
			Note note,
			List<UserPermission> userPermissions,
			Integer userId) {
		boolean isAuthor = note.getCreatedUser().getId() == userId;

		return NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(isAuthor)
				.sharedUsers(!isAuthor && !userPermissions.isEmpty()
						? userPermissions.stream()
								.map(userPermission -> UserAuthorization.builder()
										.permissionId(userPermission.getId())
										.userId(userPermission.getUser().getId())
										.type(userPermission.toPermissionType())
										.build())
								.toList()
						: List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();
	}

	public NoteDetailResponse createNoteDetailResponse(Note note, Integer userId) {
		return NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(note.getCreatedUser().getId() == userId)
				.sharedUsers(List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();
	}

	public Note createNote(User user) {
		return Note.builder()
				.title("")
				.contents("")
				.createdUser(user)
				.updatedUser(user)
				.build();
	}

	public Note updateNote(Integer noteId, NoteUpdateRequest request, User user) {
		return Note.builder()
				.id(noteId)
				.title(request.getTitle())
				.contents(request.getContents())
				.updatedUser(user)
				.build();
	}
}
