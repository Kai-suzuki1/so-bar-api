package app.diy.note_taking_app.service.factory;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoteFactory {

	private static final int previewContentsMaxLength = 175;

	public List<PreviewNoteResponse> createPreviewNoteResponseList(List<Note> notes) {
		return notes.stream()
				.map((note) -> {
					return PreviewNoteResponse.builder()
							.id(note.getId())
							.title(note.getTitle())
							// Extract beginning of 175 letters for the side menu bar
							.previewContents(
									StringUtils.isEmpty(note.getContents()) || note.getContents().length() < previewContentsMaxLength
											? note.getContents()
											: note.getContents().substring(0, previewContentsMaxLength))
							.createdAt(note.getCreatedAt())
							.createdBy(note.getCreatedUser().getName())
							.updatedAt(note.getUpdatedAt())
							.updatedBy(note.getUpdatedUser().getName())
							.build();
				})
				.toList();
	}

	public NoteDetailResponse creteNoteDetailResponse(
			Note note,
			List<UserPermission> userPermissions,
			Integer userId) {
		boolean isAuthor = note.getCreatedUser().getId() == userId;

		return NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(isAuthor)
				.sharedUsers(isAuthor && !userPermissions.isEmpty()
						? userPermissions.stream()
								.map(userPermission -> {
									return UserAuthorization.builder()
											.permissionId(userPermission.getId())
											.userId(userPermission.getUser().getId())
											.type(userPermission.toPermissionType())
											.build();
								})
								.toList()
						: List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.build();
	}

	public NoteDetailResponse creteNoteDetailResponse(Note note, Integer userId) {
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
