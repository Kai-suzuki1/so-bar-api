package app.diy.note_taking_app.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.exceptions.InsufficientUserAuthorizationException;
import app.diy.note_taking_app.exceptions.NoteNotFoundException;
import app.diy.note_taking_app.repository.NoteRepository;
import app.diy.note_taking_app.repository.UserPermissionRepository;
import app.diy.note_taking_app.service.factory.NoteFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

	private final UserPermissionRepository userPermissionRepository;
	private final NoteRepository noteRepository;
	private final NoteFactory noteFactory;

	@Override
	public List<PreviewNoteResponse> getNoteList(Integer userId) {
		List<Note> notesWrittenByUser = noteRepository
				.findByCreatedUser_IdAndDeletedFlagFalse(userId);
		List<Note> notesWrittenByOther = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(userId)
				.stream()
				.filter(userPermission -> !userPermission.getNote().isDeletedFlag())
				.map(UserPermission::getNote)
				.toList();

		if (notesWrittenByUser.isEmpty() && notesWrittenByOther.isEmpty()) {
			return List.of();
		}

		return noteFactory.createPreviewNotes(Stream
				.of(notesWrittenByUser, notesWrittenByOther)
				.flatMap(notes -> notes.stream())
				.collect(Collectors.toList()));
	}

	@Override
	public NoteDetailResponse getNoteDetail(Integer noteId, Integer userId) {
		Note note = noteRepository
				.findByIdAndDeletedFlagFalse(noteId)
				.orElseThrow(() -> new NoteNotFoundException("Note was not found"));
		List<UserPermission> userPermissions = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(noteId);

		NoteDetailResponse noteDetail = noteFactory.creteNoteDetailResponse(note, userPermissions, userId);
		// Throw exception if user is not author and does not have authorization
		if (!noteDetail.isUserIsCreator()
				&& (noteDetail.getSharedUsers().isEmpty() || noteDetail.getSharedUsers().stream()
						.filter(sharedUser -> sharedUser.getUserId() == userId)
						.toList()
						.isEmpty())) {
			throw new InsufficientUserAuthorizationException("Not allowed to access this note");
		}

		return noteDetail;
	}
}
