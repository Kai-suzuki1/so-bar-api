package app.diy.note_taking_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.diy.note_taking_app.domain.entity.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

	Optional<Note> findByIdAndDeletedFlagFalse(Integer id);

	List<Note> findByCreatedUser_IdAndDeletedFlagFalse(Integer id);

	@Modifying
	@Query("update Note set deletedFlag = true, updatedAt = now() where id in :noteIds")
	void deleteNotes(@Param("noteIds") List<Integer> noteIds);
}
