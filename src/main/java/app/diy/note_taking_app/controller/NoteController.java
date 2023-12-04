package app.diy.note_taking_app.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.service.NoteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("v1/notes")
@RequiredArgsConstructor
public class NoteController {

	private final NoteService noteService;

	@GetMapping
	public List<PreviewNoteResponse> getNotes(@AuthenticationPrincipal User user) {
		return noteService.getNotes(user.getId());
	}

	@GetMapping("/{noteId}")
	public NoteDetailResponse getNoteDetail(
			@PathVariable("noteId") Integer noteId,
			@AuthenticationPrincipal User user) {
		return noteService.getNoteDetail(noteId, user.getId());
	}
}
