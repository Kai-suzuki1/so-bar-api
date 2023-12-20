package app.diy.note_taking_app.service;

public interface UserPermissionService {

	boolean existsUndeletedPermissionById(Integer id);

	boolean canUpdateNote(Integer noteId, Integer userId);
}
