package app.diy.note_taking_app.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.repository.UserPermissionRepository;

@ExtendWith(MockitoExtension.class)
public class UserPermissionServiceTest {

	@InjectMocks
	private UserPermissionServiceImpl target;

	@Mock
	private UserPermissionRepository mockUserPermissionRepository;

	@Test
	void existsUndeletedPermissionById_ActivePermission_ReturnTrue() {
		when(mockUserPermissionRepository.existsByIdAndDeletedFlagFalse(anyInt())).thenReturn(true);
		assertTrue(target.existsUndeletedPermissionById(1));
	}

	@Test
	void existsUndeletedPermissionById_DeletedPermission_ReturnFalse() {
		when(mockUserPermissionRepository.existsByIdAndDeletedFlagFalse(anyInt())).thenReturn(false);
		assertFalse(target.existsUndeletedPermissionById(1));
	}

	@Test
	void canUpdateNote_ValidPermission_ReturnTrue() {
		Optional<UserPermission> returnVal = Optional.ofNullable(
				UserPermission.builder().type("{\"readOnly\": false, \"readWrite\": true}").build());

		when(mockUserPermissionRepository.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt(), anyInt()))
				.thenReturn(returnVal);
		assertTrue(target.canUpdateNote(anyInt(), anyInt()));
	}

	@Test
	void canUpdateNote_EmptyPermission_ReturnFalse() {
		Optional<UserPermission> returnVal = Optional.empty();

		when(mockUserPermissionRepository.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt(), anyInt()))
				.thenReturn(returnVal);
		assertFalse(target.canUpdateNote(anyInt(), anyInt()));
	}

	@Test
	void canUpdateNote_ReadOnlyPermission_ReturnFalse() {
		Optional<UserPermission> returnVal = Optional.ofNullable(
				UserPermission.builder().type("{\"readOnly\": true, \"readWrite\": false}").build());

		when(mockUserPermissionRepository.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt(), anyInt()))
				.thenReturn(returnVal);
		assertFalse(target.canUpdateNote(anyInt(), anyInt()));
	}
}
