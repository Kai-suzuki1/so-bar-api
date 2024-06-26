package app.diy.note_taking_app.controller;

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
	public Note getUndeletedNote(@PathVariable("noteId") Integer noteId) {
		return noteService.getUndeletedNote(noteId);
	}

	@GetMapping
	public NoteDetailResponse getNoteDetail(@PathVariable("noteId") Integer noteId, @AuthenticationPrincipal User user) {
		NoteDetailResponse noteDetail = noteService.getNoteDetail(noteId, user.getId());
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
			Note note,
			@Validated @RequestBody NoteUpdateRequest request,
			@AuthenticationPrincipal User user) {
		validateUserAuthorization(user.getId(), note);

		return noteService.update(note.getId(), request, user);
	}

	@PatchMapping("/delete")
	public void deleteNote(Note note, @AuthenticationPrincipal User user) {
		validateUserAuthorization(user.getId(), note);

		noteService.delete(note, user);
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
}
