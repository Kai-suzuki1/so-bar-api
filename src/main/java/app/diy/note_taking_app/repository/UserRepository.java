package app.diy.note_taking_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.diy.note_taking_app.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByIdAndDeletedFlagFalse(int id);

	Optional<User> findByEmail(String username);

	Optional<User> findByEmailAndDeletedFlagFalse(String email);

	boolean existsByEmailAndDeletedFlagFalse(String email);

	boolean existsByNameAndDeletedFlagFalse(String username);

	@Modifying
	@Query("update User set deletedFlag = true, updatedAt = now() where id = :userId")
	void deleteUser(@Param("userId") Integer userId);
}
