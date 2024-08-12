package app.diy.note_taking_app.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import app.diy.note_taking_app.configuration.JPAAuditingConfiguration;
import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;

@DataJpaTest(showSql = true)
@Import(JPAAuditingConfiguration.class)
public class NoteRepositoryTest {

	@Autowired
	NoteRepository noteRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Note insertedNote;

	private Note savedNote;

	private List<User> savedUsers;

	@BeforeEach
	void setUp() {
		savedUsers = userRepository.saveAll(List.of(
				User.builder()
						.name("Create User")
						.email("create_user@gmail.com")
						.password("create")
						.role(Role.USER)
						.image("Test".getBytes())
						.deletedFlag(false)
						.build(),
				User.builder()
						.name("Update User")
						.email("update_user@gmail.com")
						.password("update")
						.role(Role.USER)
						.image("Test".getBytes())
						.deletedFlag(false)
						.build()));

		insertedNote = Note.builder()
				.title("Test title")
				.contents("Test contents")
				.deletedFlag(false)
				.createdUser(savedUsers.get(0))
				.updatedUser(savedUsers.get(1))
				.build();
		savedNote = noteRepository.save(insertedNote);
	}

	@Test
	void findByCreatedUser_Id_ReturnsSingleNote() {
		List<Note> returnVal = noteRepository.findByCreatedUser_Id(savedNote.getCreatedUser().getId());

		assertFalse(returnVal.isEmpty(), "Note should be found by id of created user");
		assertEquals(returnVal.size(), 1);
		assertEquals(savedNote, returnVal.stream().findFirst().get());
	}

	@Test
	void findByCreatedUser_Id_ReturnsNoteList() {
		Note secondInsertedNote = Note.builder()
				.createdUser(savedUsers.get(0))
				.updatedUser(savedUsers.get(1))
				.build();
		noteRepository.saveAndFlush(secondInsertedNote);

		List<Note> returnVal = noteRepository.findByCreatedUser_Id(savedNote.getCreatedUser().getId());

		assertFalse(returnVal.isEmpty(), "Note should be found by id of created user");
		assertEquals(returnVal.size(), 2);
		assertArrayEquals(new Note[] { insertedNote, secondInsertedNote }, returnVal.toArray());
	}

	@Test
	void findByCreatedUser_Id_InexistentId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		List<Note> returnVal = noteRepository.findByCreatedUser_Id(savedUsers.get(savedUsers.size() - 1).getId() + 1);

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "Note should be empty by id of created user");
	}

	@Test
	void findByCreatedUser_IdAndDeletedFlagFalse_ReturnsSingleNote() {
		List<Note> returnVal = noteRepository.findByCreatedUser_IdAndDeletedFlagFalse(savedNote.getCreatedUser().getId());

		assertFalse(returnVal.isEmpty(), "Note should be found by id of created user");
		assertEquals(returnVal.size(), 1);
		assertEquals(savedNote, returnVal.stream().findFirst().get());
	}

	@Test
	void findByCreatedUser_IdAndDeletedFlagFalse_ReturnsNoteList() {
		Note secondInsertedNote = Note.builder()
				.createdUser(savedUsers.get(0))
				.updatedUser(savedUsers.get(1))
				.build();
		noteRepository.saveAndFlush(secondInsertedNote);

		List<Note> returnVal = noteRepository.findByCreatedUser_IdAndDeletedFlagFalse(savedNote.getCreatedUser().getId());

		assertFalse(returnVal.isEmpty(), "Note should be found by id of created user");
		assertEquals(returnVal.size(), 2);
		assertArrayEquals(new Note[] { insertedNote, secondInsertedNote }, returnVal.toArray());
	}

	@Test
	void findByCreatedUser_IdAndDeletedFlagFalse_InexistentId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		List<Note> returnVal = noteRepository
				.findByCreatedUser_IdAndDeletedFlagFalse(savedUsers.get(savedUsers.size() - 1).getId() + 1);

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "Note should be empty by id of created user because of inexistent id");
	}

	@Test
	void findByCreatedUser_IdAndDeletedFlagFalse_Deleted_ReturnsEmptyList() {
		savedNote.setDeletedFlag(true);
		noteRepository.save(savedNote);
		List<Note> returnVal = noteRepository.findByCreatedUser_IdAndDeletedFlagFalse(savedNote.getCreatedUser().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "Note should be empty by id of created user because it is deleted");
	}

	@Test
	void deleteNote_DeleteNote() {
		noteRepository.deleteNote(savedNote.getId(), savedNote.getCreatedUser());
		entityManager.refresh(savedNote);
		Optional<Note> actual = noteRepository.findById(savedNote.getId());
		List<Note> allRecords = noteRepository.findAll();

		assertEquals(1, allRecords.size(), "Number of record should not be changed");
		assertTrue(actual.get().isDeletedFlag(), "DeletedFlag should be true");
		assertEquals(actual.get().getCreatedUser(), actual.get().getUpdatedUser(), "UpdatedUser should be updated");
		assertNotEquals(actual.get().getCreatedAt(), actual.get().getUpdatedAt(), "UpdatedAt was not updated");
	}

	@Test
	void deleteNote_DeleteDifferentNote() {
		noteRepository.deleteNote(savedNote.getId() + 1, savedNote.getCreatedUser());
		entityManager.refresh(savedNote);
		Optional<Note> actual = noteRepository.findById(savedNote.getId());

		assertFalse(actual.get().isDeletedFlag(), "DeletedFlag should be false");
		assertEquals(savedNote, actual.get(), "Note should not be updated");
	}

	@Test
	void deleteNotes_DeleteNotes() {
		// Insert another note
		Note addedNote = noteRepository.saveAndFlush(Note.builder()
				.title("Test title2")
				.contents("Test contents2")
				.deletedFlag(false)
				.createdUser(savedUsers.get(0))
				.updatedUser(savedUsers.get(1))
				.build());
		noteRepository.deleteNotes(List.of(savedNote.getId(), addedNote.getId()), savedNote.getCreatedUser());
		entityManager.refresh(savedNote);
		entityManager.refresh(addedNote);
		Optional<Note> savedNoteActual = noteRepository.findById(savedNote.getId());
		Optional<Note> addedNoteActual = noteRepository.findById(addedNote.getId());
		List<Note> allRecords = noteRepository.findAll();

		assertEquals(2, allRecords.size(), "Number of records should not be changed");
		assertAll(
				() -> assertTrue(savedNoteActual.get().isDeletedFlag(), "DeletedFlag should be true"),
				() -> assertEquals(savedNoteActual.get().getCreatedUser(), savedNoteActual.get().getUpdatedUser(),
						"UpdatedUser should be updated"),
				() -> assertNotEquals(savedNoteActual.get().getCreatedAt(), savedNoteActual.get().getUpdatedAt(),
						"UpdatedAt was not updated"));
		assertAll(
				() -> assertTrue(addedNoteActual.get().isDeletedFlag(), "DeletedFlag should be true"),
				() -> assertEquals(addedNoteActual.get().getCreatedUser(), addedNoteActual.get().getUpdatedUser(),
						"UpdatedUser should be updated"),
				() -> assertNotEquals(addedNoteActual.get().getCreatedAt(), addedNoteActual.get().getUpdatedAt(),
						"UpdatedAt was not updated"));

	}

	@Test
	void deleteNotes_DeleteDifferentNotes() {
		noteRepository.deleteNotes(List.of(savedNote.getId() + 1), savedNote.getCreatedUser());
		entityManager.refresh(savedNote);
		Optional<Note> actual = noteRepository.findById(savedNote.getId());

		assertFalse(actual.get().isDeletedFlag(), "DeletedFlag should be false");
		assertEquals(savedNote, actual.get(), "Note should not be updated");
	}

	@Test
	void deleteNotes_PassedEmptyNoteIds() {
		noteRepository.deleteNotes(List.of(), savedNote.getCreatedUser());
		entityManager.refresh(savedNote);
		Optional<Note> actual = noteRepository.findById(savedNote.getId());

		assertFalse(actual.get().isDeletedFlag(), "DeletedFlag should be false");
		assertEquals(savedNote, actual.get(), "Note should not be updated");
	}
}
