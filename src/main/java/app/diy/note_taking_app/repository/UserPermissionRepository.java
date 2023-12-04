package app.diy.note_taking_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.diy.note_taking_app.domain.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Integer> {

	public List<UserPermission> findByUser_IdAndDeletedFlagFalse(Integer userId);

	public List<UserPermission> findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(Integer userId);

	public List<UserPermission> findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(Integer noteId);

	@Modifying
	@Query("update UserPermission set deletedFlag = true where id in :userPermissionIds")
	void deleteUserPermissions(@Param("userPermissionIds") List<Integer> userPermissionIds);
}
