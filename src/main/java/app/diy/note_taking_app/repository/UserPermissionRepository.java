package app.diy.note_taking_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Integer> {

	public List<UserPermission> findByUser_IdAndDeletedFlagFalse(Integer userId);

	public List<UserPermission> findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(Integer userId);

	public List<UserPermission> findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(Integer noteId);

	public Optional<UserPermission> findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(
			Integer noteId,
			Integer userId);

	public boolean existsByIdAndDeletedFlagFalse(Integer id);

	public boolean existsByNote(Note note);

	@Modifying
	@Query("update UserPermission set deletedFlag = true where id in :userPermissionIds")
	void deleteUserPermissionsByIds(@Param("userPermissionIds") List<Integer> userPermissionIds);

	@Modifying
	@Query("update UserPermission set deletedFlag = true where note = :note")
	void deleteUserPermissionsByNote(@Param("note") Note note);
}
