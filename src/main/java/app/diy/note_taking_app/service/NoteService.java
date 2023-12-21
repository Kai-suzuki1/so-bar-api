package app.diy.note_taking_app.service;

import java.util.List;

import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;

public interface NoteService {

	List<PreviewNoteResponse> getNoteList(Integer userId);

	Note getUndeletedNote(Integer noteId);

	NoteDetailResponse getNoteDetail(Note note, Integer userId);

	NoteDetailResponse create(User user);

	NoteDetailResponse update(Integer noteId, NoteUpdateRequest request, User user);

	void delete(Note note, User user);
}
