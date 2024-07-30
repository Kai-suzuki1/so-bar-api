package app.diy.note_taking_app.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.InsufficientUserAuthorizationException;
import app.diy.note_taking_app.exceptions.NoteNotFoundException;
import app.diy.note_taking_app.service.NoteService;
import app.diy.note_taking_app.service.UserPermissionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("v1/notes/{noteId}")
@RequiredArgsConstructor
public class NoteByIdController {

	private final NoteService noteService;
	private final UserPermissionService userPermissionService;

	@ModelAttribute
	public Optional<Note> getUndeletedNote(@PathVariable("noteId") Integer noteId) {
		return noteService.getNote(noteId);
	}

	@GetMapping
	public NoteDetailResponse getNoteDetail(
			Optional<Note> note,
			@PathVariable("noteId") Integer noteId,
			@AuthenticationPrincipal User user) {

		NoteDetailResponse noteDetail = noteService.getNoteDetail(
				note.orElseThrow(() -> new NoteNotFoundException("Note was not found")),
				user.getId());
		// Throw exception if user is not author and does not have authorization
		if (!noteDetail.isUserIsAuthor()
				&& (noteDetail.getSharedUsers().isEmpty() || noteDetail.getSharedUsers().stream()
						.filter(sharedUser -> sharedUser.getUserId() == user.getId())
						.toList()
						.isEmpty())) {
			throw new InsufficientUserAuthorizationException("Not allowed to access this note");
		}

		return noteDetail;
	}

	@PatchMapping
	public NoteDetailResponse updateNote(
			Optional<Note> note,
			@Validated @RequestBody NoteUpdateRequest request,
			@AuthenticationPrincipal User user) {
		Note targetNote = validateNoteExistence(note);
		validateUserAuthorization(user.getId(), targetNote);

		return noteService.update(targetNote.getId(), request, user);
	}

	@PatchMapping("/delete")
	public void deleteNote(Optional<Note> note, @AuthenticationPrincipal User user) {
		Note targetNote = validateNoteExistence(note);
		validateUserAuthorization(user.getId(), targetNote);

		noteService.delete(targetNote, user);
	}

	/**
	 * if user is shared user, check the permission and throw
	 * {@link InsufficientUserAuthorizationException}
	 * 
	 * @param userId
	 * @param note
	 * @exception InsufficientUserAuthorizationException
	 */
	private void validateUserAuthorization(Integer userId, Note note) {
		if (!userId.equals(note.getCreatedUser().getId())
				&& !userPermissionService.canUpdateNote(note.getId(), userId)) {
			throw new InsufficientUserAuthorizationException("Not allowed to update this note");
		}
	}

	/**
	 * if note is present and not deleted, returns {@code Note}, otherwise
	 * {@code NoteNotFoundException}
	 * 
	 * @param note {@code Optional<Note>} the value could be {@code Optional.empty}
	 * @return {@code Note}
	 * @throws NoteNotFoundException
	 */
	private Note validateNoteExistence(Optional<Note> note) {
		if (note.isPresent() && !note.get().isDeletedFlag()) {
			return note.get();
		}
		throw new NoteNotFoundException("Note was not found");
	}
}
