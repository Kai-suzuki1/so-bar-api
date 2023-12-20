package app.diy.note_taking_app.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.exceptions.NoteEntityTransactionalException;
import app.diy.note_taking_app.exceptions.NoteNotFoundException;
import app.diy.note_taking_app.repository.NoteRepository;
import app.diy.note_taking_app.repository.UserPermissionRepository;
import app.diy.note_taking_app.service.factory.NoteFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

	private final UserPermissionRepository userPermissionRepository;
	private final NoteRepository noteRepository;
	private final NoteFactory noteFactory;
	private final EntityManager entityManager;

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

		return noteFactory.createPreviewNoteResponseList(Stream
				.of(notesWrittenByUser, notesWrittenByOther)
				.flatMap(notes -> notes.stream())
				.collect(Collectors.toList()));
	}

	@Override
	public Note getUndeletedNote(Integer noteId) {
		return noteRepository.findByIdAndDeletedFlagFalse(noteId)
				.orElseThrow(() -> new NoteNotFoundException("Note was not found"));
	}

	@Override
	public NoteDetailResponse getNoteDetail(Note note, Integer userId) {
		List<UserPermission> userPermissions = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(note.getId());

		return noteFactory.creteNoteDetailResponse(note, userPermissions, userId);
	}

	@Override
	@Transactional
	public NoteDetailResponse create(User user) {
		try {
			Note savedNote = noteRepository.save(noteFactory.createNote(user));
			return noteFactory.creteNoteDetailResponse(savedNote, user.getId());
		} catch (Exception e) {
			throw new NoteEntityTransactionalException("Failed to save note", e);
		}
	}

	@Override
	@Transactional
	public NoteDetailResponse update(Integer noteId, NoteUpdateRequest request, User user) {
		try {
			Note savedNote = noteRepository.saveAndFlush(noteFactory.updateNote(noteId, request, user));
			// Refresh DB instance and synchronize to updated DB data
			entityManager.refresh(savedNote);
			return noteFactory.creteNoteDetailResponse(
					savedNote,
					userPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(noteId),
					user.getId());
		} catch (Exception e) {
			throw new NoteEntityTransactionalException("Failed to update note", e);
		}
	}
}
