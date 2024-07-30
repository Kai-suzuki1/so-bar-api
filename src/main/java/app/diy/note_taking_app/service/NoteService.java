package app.diy.note_taking_app.service;

import java.util.List;
import java.util.Optional;

import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;

public interface NoteService {

	List<PreviewNoteResponse> getNoteList(Integer userId);

	Optional<Note> getNote(Integer noteId);

	NoteDetailResponse getNoteDetail(Note note, Integer userId);

	NoteDetailResponse create(User user);

	NoteDetailResponse update(Integer noteId, NoteUpdateRequest request, User user);

	void delete(Note note, User user);
}
