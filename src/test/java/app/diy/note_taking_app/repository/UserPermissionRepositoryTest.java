package app.diy.note_taking_app.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import app.diy.note_taking_app.Util.StringUtil;
import app.diy.note_taking_app.configuration.JPAAuditingConfiguration;
import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.dto.PermissionType;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;

@DataJpaTest(showSql = true)
@Import(JPAAuditingConfiguration.class)
public class UserPermissionRepositoryTest {

	@Autowired
	UserPermissionRepository userPermissionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	NoteRepository noteRepository;

	@Autowired
	private TestEntityManager entityManager;

	UserPermission insertedUserPermission;

	UserPermission savedUserPermission;

	private Note savedNote;

	private List<User> savedUsers;

	private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

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

		savedNote = noteRepository.save(Note.builder()
				.title("Test title")
				.contents("Test contents")
				.deletedFlag(false)
				.createdUser(savedUsers.get(0))
				.updatedUser(savedUsers.get(1))
				.build());

		insertedUserPermission = UserPermission.builder()
				.note(savedNote)
				.user(savedUsers.get(0))
				.hash("PermissionHash".getBytes())
				.type(StringUtil.convertJsonToString(
						PermissionType.builder()
								.readOnly(true)
								.readWrite(false)
								.build(),
						objectMapper))
				.invitedUserName(savedUsers.get(0).getName())
				.deletedFlag(false)
				.acceptedFlag(true)
				.build();
		savedUserPermission = userPermissionRepository.save(insertedUserPermission);
	}

	@Test
	void findByUser_IdAndDeletedFlagFalse_ReturnsSingleNote() {
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalse(savedUserPermission.getUser().getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of user");
		assertEquals(returnVal.size(), 1);
		assertEquals(savedUserPermission, returnVal.stream().findFirst().get());
	}

	@Test
	void findByUser_IdAndDeletedFlagFalse_ReturnsNoteList() {
		UserPermission secondInsertedUserPermission = UserPermission.builder()
				.note(savedNote)
				.user(savedUsers.get(0))
				.hash("PermissionHash2".getBytes())
				.type(StringUtil.convertJsonToString(
						PermissionType.builder()
								.readOnly(true)
								.readWrite(false)
								.build(),
						objectMapper))
				.invitedUserName(savedUsers.get(0).getName())
				.deletedFlag(false)
				.acceptedFlag(true)
				.build();
		userPermissionRepository.saveAndFlush(secondInsertedUserPermission);

		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalse(savedUsers.get(0).getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of user");
		assertEquals(returnVal.size(), 2);
		assertArrayEquals(new UserPermission[] { insertedUserPermission, secondInsertedUserPermission },
				returnVal.toArray());
	}

	@Test
	void findByUser_IdAndDeletedFlagFalse_InexistentId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalse(savedUsers.get(savedUsers.size() - 1).getId() + 1);

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of user");

	}

	@Test
	void findByUser_IdAndDeletedFlagFalse_Deleted_ReturnsEmptyList() {
		savedUserPermission.setDeletedFlag(true);
		userPermissionRepository.save(savedUserPermission);
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalse(savedUserPermission.getUser().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of user because it is deleted");
	}

	@Test
	void findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_ReturnsSingleNote() {
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUserPermission.getUser().getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of user");
		assertEquals(returnVal.size(), 1);
		assertEquals(savedUserPermission, returnVal.stream().findFirst().get());
	}

	@Test
	void findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_ReturnsNoteList() {
		UserPermission secondInsertedUserPermission = UserPermission.builder()
				.note(savedNote)
				.user(savedUsers.get(0))
				.hash("PermissionHash2".getBytes())
				.type(StringUtil.convertJsonToString(
						PermissionType.builder()
								.readOnly(true)
								.readWrite(false)
								.build(),
						objectMapper))
				.invitedUserName(savedUsers.get(0).getName())
				.deletedFlag(false)
				.acceptedFlag(true)
				.build();
		userPermissionRepository.saveAndFlush(secondInsertedUserPermission);

		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUsers.get(0).getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of user");
		assertEquals(returnVal.size(), 2);
		assertArrayEquals(new UserPermission[] { insertedUserPermission, secondInsertedUserPermission },
				returnVal.toArray());
	}

	@Test
	void findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_InexistentId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUsers.get(savedUsers.size() - 1).getId() + 1);

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of user");

	}

	@Test
	void findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_Deleted_ReturnsEmptyList() {
		savedUserPermission.setDeletedFlag(true);
		userPermissionRepository.save(savedUserPermission);
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUserPermission.getUser().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of user because it is deleted");
	}

	@Test
	void findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_Denied_ReturnsEmptyList() {
		savedUserPermission.setAcceptedFlag(false);
		userPermissionRepository.save(savedUserPermission);
		List<UserPermission> returnVal = userPermissionRepository
				.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUserPermission.getUser().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of user because it is denied");
	}

	@Test
	void findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue_ReturnsSingleNote() {
		List<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUserPermission.getNote().getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of note");
		assertEquals(returnVal.size(), 1);
		assertEquals(savedUserPermission, returnVal.stream().findFirst().get());
	}

	@Test
	void findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue_ReturnsNoteList() {
		UserPermission secondInsertedUserPermission = UserPermission.builder()
				.note(savedNote)
				.user(savedUsers.get(0))
				.hash("PermissionHash2".getBytes())
				.type(StringUtil.convertJsonToString(
						PermissionType.builder()
								.readOnly(true)
								.readWrite(false)
								.build(),
						objectMapper))
				.invitedUserName(savedUsers.get(0).getName())
				.deletedFlag(false)
				.acceptedFlag(true)
				.build();
		userPermissionRepository.saveAndFlush(secondInsertedUserPermission);

		List<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedNote.getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of note");
		assertEquals(returnVal.size(), 2);
		assertArrayEquals(new UserPermission[] { insertedUserPermission, secondInsertedUserPermission },
				returnVal.toArray());
	}

	@Test
	void findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue_InexistentId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		List<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedNote.getId() + 1);

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note");

	}

	@Test
	void findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue_Deleted_ReturnsEmptyList() {
		savedUserPermission.setDeletedFlag(true);
		userPermissionRepository.save(savedUserPermission);
		List<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUserPermission.getNote().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note because it is deleted");
	}

	@Test
	void findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue_Denied_ReturnsEmptyList() {
		savedUserPermission.setAcceptedFlag(false);
		userPermissionRepository.save(savedUserPermission);
		List<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(savedUserPermission.getNote().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note because it is denied");
	}

	@Test
	void findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_ReturnsSingleNote() {
		Optional<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(
						savedUserPermission.getNote().getId(),
						savedUserPermission.getUser().getId());

		assertFalse(returnVal.isEmpty(), "UserPermission should be found by id of note and user");
		assertEquals(savedUserPermission, returnVal.get());
	}

	@Test
	void findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_InexistentNoteId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		Optional<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(
						savedNote.getId() + 1,
						savedUsers.get(0).getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note and user");
	}

	@Test
	void findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_InexistentUserId_ReturnsEmptyList() {
		// pass last element of savedUsers' id and add 1 for inexistent index to find
		Optional<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(
						savedNote.getId(),
						savedUsers.get(savedUsers.size() - 1).getId() + 1);

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note and user");
	}

	@Test
	void findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_Deleted_ReturnsEmptyList() {
		savedUserPermission.setDeletedFlag(true);
		userPermissionRepository.save(savedUserPermission);
		Optional<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(
						savedUserPermission.getNote().getId(),
						savedUserPermission.getUser().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note and user because it is deleted");
	}

	@Test
	void findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue_Denied_ReturnsEmptyList() {
		savedUserPermission.setAcceptedFlag(false);
		userPermissionRepository.save(savedUserPermission);
		Optional<UserPermission> returnVal = userPermissionRepository
				.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(
						savedUserPermission.getNote().getId(),
						savedUserPermission.getUser().getId());

		assertNotNull(returnVal);
		assertTrue(returnVal.isEmpty(), "UserPermission should be empty by id of note and user because it is denied");
	}

	@Test
	void existsByIdAndDeletedFlagFalse_ReturnsTrue() {
		boolean returnVal = userPermissionRepository.existsByIdAndDeletedFlagFalse(savedUserPermission.getId());

		assertNotNull(returnVal);
		assertTrue(returnVal, "UserPermission should be found by id and should not be deleted");
	}

	@Test
	void existsByIdAndDeletedFlagFalse_InexistentId_ReturnsFalse() {
		boolean returnVal = userPermissionRepository.existsByIdAndDeletedFlagFalse(savedUserPermission.getId() + 1);

		assertNotNull(returnVal);
		assertFalse(returnVal, "Value should be false because the id should be inexistent");
	}

	@Test
	void existsByIdAndDeletedFlagFalse_Deleted_ReturnsFalse() {
		savedUserPermission.setDeletedFlag(true);
		userPermissionRepository.save(savedUserPermission);
		boolean returnVal = userPermissionRepository.existsByIdAndDeletedFlagFalse(savedUserPermission.getId());

		assertNotNull(returnVal);
		assertFalse(returnVal, "Value should be false because the UserPermission should be deleted");
	}

	@Test
	void existsByNote_ReturnsTrue() {
		boolean returnVal = userPermissionRepository.existsByNote(savedUserPermission.getNote());

		assertNotNull(returnVal);
		assertTrue(returnVal, "UserPermission should be found by id of note");
	}

	@Test
	void existsByNote_InexistentId_ReturnsFalse() {
		boolean returnVal = userPermissionRepository.existsByNote(Note.builder().id(savedNote.getId() + 1).build());

		assertNotNull(returnVal);
		assertFalse(returnVal, "Value should be false because the id of note should be inexistent");
	}

	@Test
	void deleteUserPermissionsByIds_DeleteNotes() throws Exception {
		// Insert another permission
		UserPermission addedUserPermission = userPermissionRepository.saveAndFlush(UserPermission.builder()
				.note(savedNote)
				.user(savedUsers.get(0))
				.hash("PermissionHash".getBytes())
				.type(StringUtil.convertJsonToString(
						PermissionType.builder()
								.readOnly(true)
								.readWrite(false)
								.build(),
						objectMapper))
				.invitedUserName(savedUsers.get(0).getName())
				.deletedFlag(false)
				.acceptedFlag(true)
				.build());
		userPermissionRepository
				.deleteUserPermissionsByIds(List.of(savedUserPermission.getId(), addedUserPermission.getId()));
		entityManager.refresh(savedUserPermission);
		entityManager.refresh(addedUserPermission);
		Optional<UserPermission> savedNoteActual = userPermissionRepository.findById(savedUserPermission.getId());
		Optional<UserPermission> addedNoteActual = userPermissionRepository.findById(addedUserPermission.getId());
		List<UserPermission> allRecords = userPermissionRepository.findAll();

		assertEquals(2, allRecords.size(), "Number of records should not be changed");
		assertAll(
				() -> assertTrue(
						savedNoteActual.get().isDeletedFlag(),
						"DeletedFlag should be true"),
				() -> assertTrue(
						addedNoteActual.get().isDeletedFlag(),
						"DeletedFlag should be true"));
	}

	@Test
	void deleteUserPermissionsByIds_DeleteDifferentNotes() {
		userPermissionRepository.deleteUserPermissionsByIds(List.of(savedUserPermission.getId() + 1));
		entityManager.refresh(savedUserPermission);
		Optional<UserPermission> actual = userPermissionRepository.findById(savedUserPermission.getId());

		assertFalse(actual.get().isDeletedFlag(), "DeletedFlag should be false");
		assertEquals(savedUserPermission, actual.get(), "Note should not be updated");
	}

	@Test
	void deleteUserPermissionsByIds_PassedEmptyNoteIds() {
		userPermissionRepository.deleteUserPermissionsByIds(List.of());
		entityManager.refresh(savedUserPermission);
		Optional<UserPermission> actual = userPermissionRepository.findById(savedUserPermission.getId());

		assertFalse(actual.get().isDeletedFlag(), "DeletedFlag should be false");
		assertEquals(savedUserPermission, actual.get(), "UserPermission should not be updated");
	}

}
