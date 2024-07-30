package app.diy.note_taking_app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import app.diy.note_taking_app.configuration.JPAAuditingConfiguration;
import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.entity.User;

@DataJpaTest(showSql = true)
@Import(JPAAuditingConfiguration.class)
public class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	private User testUser;

	private User savedUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.name("Test Name")
				.email("test_email@gmail.com")
				.password("sample")
				.role(Role.USER)
				.image("Test".getBytes())
				.deletedFlag(false)
				.build();

		savedUser = userRepository.saveAndFlush(testUser);
	}

	@Test
	void findByIdAndDeletedFlagFalse_ReturnsUser() {
		Optional<User> returnVal = userRepository.findByIdAndDeletedFlagFalse(savedUser.getId());

		assertTrue(returnVal.isPresent(), "User should be found by id unless it is not deleted");
		assertEquals(savedUser, returnVal.get());
	}

	@Test
	void findByIdAndDeletedFlagFalse_InexistentId_ReturnsEmpty() {
		Optional<User> returnVal = userRepository.findByIdAndDeletedFlagFalse(savedUser.getId() + 1);

		assertTrue(returnVal.isEmpty(), "User was not found");
	}

	@Test
	void findByIdAndDeletedFlagFalse_DeletedNote_ReturnsEmpty() {
		// Set deletedFlag=true
		savedUser.setDeletedFlag(true);
		userRepository.save(savedUser);

		Optional<User> returnVal = userRepository.findByIdAndDeletedFlagFalse(savedUser.getId());

		assertTrue(returnVal.isEmpty(), "User was not found");
	}

	@Test
	void findByEmail_ReturnsUser() {
		Optional<User> returnVal = userRepository.findByEmail(savedUser.getEmail());

		assertTrue(returnVal.isPresent(), "User should be found by Email");
		assertEquals(savedUser, returnVal.get());
	}

	@Test
	void findByEmail_InexistentEmail_ReturnsEmpty() {
		Optional<User> returnVal = userRepository.findByEmail("not_found_email@gmail.com");

		assertTrue(returnVal.isEmpty(), "Email was not found");
	}

	@Test
	void findByEmailAndDeletedFlagFalse_ReturnsUser() {
		Optional<User> returnVal = userRepository.findByEmailAndDeletedFlagFalse(savedUser.getEmail());

		assertTrue(returnVal.isPresent(), "User should be found by email unless it is not deleted");
		assertEquals(savedUser, returnVal.get());
	}

	@Test
	void findByEmailAndDeletedFlagFalse_InexistentEmail_ReturnsEmpty() {
		Optional<User> returnVal = userRepository.findByEmailAndDeletedFlagFalse("not_found_email@gmail.com");

		assertTrue(returnVal.isEmpty(), "User was not found");
	}

	@Test
	void findByEmailAndDeletedFlagFalse_DeletedNote_ReturnsEmpty() {
		// Set deletedFlag=true
		savedUser.setDeletedFlag(true);
		userRepository.save(savedUser);

		Optional<User> returnVal = userRepository.findByEmailAndDeletedFlagFalse(savedUser.getEmail());
		assertTrue(returnVal.isEmpty(), "User was not found");
	}

	@Test
	void existsByEmailAndDeletedFlagFalse_ReturnsTrue() {
		boolean returnVal = userRepository.existsByEmailAndDeletedFlagFalse(savedUser.getEmail());

		assertTrue(returnVal, "User should be found by email unless it is not deleted");
	}

	@Test
	void existsByEmailAndDeletedFlagFalse_InexistentEmail_ReturnsFalse() {
		boolean returnVal = userRepository.existsByEmailAndDeletedFlagFalse("not_found_email@gmail.com");

		assertFalse(returnVal, "User was not found");
	}

	@Test
	void existsByEmailAndDeletedFlagFalse_DeletedNote_ReturnsFalse() {
		// Set deletedFlag=true
		savedUser.setDeletedFlag(true);
		userRepository.save(savedUser);

		boolean returnVal = userRepository.existsByEmailAndDeletedFlagFalse(savedUser.getEmail());
		assertFalse(returnVal, "User was not found");
	}

	@Test
	void existsByNameAndDeletedFlagFalse_ReturnsTrue() {
		boolean returnVal = userRepository.existsByNameAndDeletedFlagFalse(savedUser.getName());

		assertTrue(returnVal, "User should be found by email unless it is not deleted");
	}

	@Test
	void existsByNameAndDeletedFlagFalse_InexistentName_ReturnsFalse() {
		boolean returnVal = userRepository.existsByNameAndDeletedFlagFalse("Not Found Name");

		assertFalse(returnVal, "User was not found");
	}

	@Test
	void existsByNameAndDeletedFlagFalse_DeletedNote_ReturnsFalse() {
		// Set deletedFlag=true
		savedUser.setDeletedFlag(true);
		userRepository.save(savedUser);

		boolean returnVal = userRepository.existsByNameAndDeletedFlagFalse(savedUser.getName());
		assertFalse(returnVal, "User was not found");
	}

	@Test
	void deleteUser_DeleteNote() {
		userRepository.deleteUser(savedUser.getId());
		entityManager.refresh(savedUser);
		Optional<User> actual = userRepository.findById(savedUser.getId());

		assertTrue(actual.get().isDeletedFlag(), "DeletedFlag should be true");
		assertNotEquals(actual.get().getCreatedAt(), actual.get().getUpdatedAt(), "UpdatedAt is not updated");
	}

	@Test
	void deleteUser_DeleteDifferentNote() {
		userRepository.deleteUser(savedUser.getId() + 1);
		entityManager.refresh(savedUser);
		Optional<User> actual = userRepository.findById(savedUser.getId());

		assertFalse(actual.get().isDeletedFlag(), "DeletedFlag should be false");
		assertEquals(savedUser, actual.get(), "UpdatedAt is not updated");
	}
}
