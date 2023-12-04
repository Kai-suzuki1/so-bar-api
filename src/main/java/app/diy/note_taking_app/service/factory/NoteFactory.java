package app.diy.note_taking_app.service.factory;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.diy.note_taking_app.domain.dto.PermissionType;
import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.exceptions.JsonConversionFailureException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoteFactory {

	private static final int previewContentsMaxLength = 175;
	private final ObjectMapper objectMapper;

	public List<PreviewNoteResponse> createPreviewNotes(List<Note> notes) {
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
		return NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsCreator(note.getCreatedUser().getId() == userId)
				.sharedUsers(userPermissions.isEmpty()
						? List.of()
						: userPermissions.stream()
								.map(userPermission -> {
									return UserAuthorization.builder()
											.permissionId(userPermission.getId())
											.userId(userPermission.getUser().getId())
											.type(convertToAuthorizationType(userPermission.getType()))
											.build();
								})
								.toList())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.build();
	}

	private PermissionType convertToAuthorizationType(String type) {
		try {
			return objectMapper.readValue(type, PermissionType.class);
		} catch (Exception e) {
			throw new JsonConversionFailureException("Failed to process JSON conversion", e);
		}
	}
}
