package app.diy.note_taking_app.service;

import java.util.List;

import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;

public interface NoteService {

	public List<PreviewNoteResponse> getNotes(Integer userId);

	public NoteDetailResponse getNoteDetail(Integer noteId, Integer userId);
}