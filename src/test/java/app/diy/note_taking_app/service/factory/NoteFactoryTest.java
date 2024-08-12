package app.diy.note_taking_app.service.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.repository.UserPermissionRepository;

@ExtendWith(MockitoExtension.class)
public class NoteFactoryTest {

	@InjectMocks
	private NoteFactory target;

	@Mock
	private UserPermissionRepository mockUserPermissionRepository;

	@Test
	void createPreviewNoteResponseList_ActiveNote_UserIsAuthor() {
		Integer userId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(userId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		List<PreviewNoteResponse> expected = List.of(PreviewNoteResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.deletableFlag(true)
				.build());

		assertEquals(expected, target.createPreviewNoteResponseList(List.of(note), userId));
	}

	@Test
	void createPreviewNoteResponseList_ActiveNote_SharedUserWithReadWritePermission() {
		Integer createdUserId = 1;
		Integer sharedUserId = 999;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		List<PreviewNoteResponse> expected = List.of(PreviewNoteResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.deletableFlag(true)
				.build());

		when(mockUserPermissionRepository.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(note.getId(),
				sharedUserId))
				.thenReturn(Optional.ofNullable(UserPermission.builder()
						.type("{\"readOnly\": false, \"readWrite\": true}")
						.build()));

		assertEquals(expected, target.createPreviewNoteResponseList(List.of(note), sharedUserId));
	}

	@Test
	void createPreviewNoteResponseList_DeletedNote() {
		Integer userId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(true)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(userId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		List<PreviewNoteResponse> expected = List.of(PreviewNoteResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.deletableFlag(false)
				.build());

		assertEquals(expected, target.createPreviewNoteResponseList(List.of(note), userId));
	}

	@Test
	void createPreviewNoteResponseList_ActiveNote_SharedUserWithReadOnlyPermission() {
		Integer createdUserId = 1;
		Integer sharedUserId = 999;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		List<PreviewNoteResponse> expected = List.of(PreviewNoteResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.deletableFlag(false)
				.build());

		when(mockUserPermissionRepository.findByNote_IdAndUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(note.getId(),
				sharedUserId))
				.thenReturn(Optional.ofNullable(UserPermission.builder()
						.type("{\"readOnly\": true, \"readWrite\": false}")
						.build()));

		assertEquals(expected, target.createPreviewNoteResponseList(List.of(note), sharedUserId));
	}

	@Test
	void createNoteDetailResponse_ThreeArgs_UserIsAuthor() {
		Integer createdUserId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(true)
				.sharedUsers(List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();

		assertEquals(expected, target.createNoteDetailResponse(note, List.of(), createdUserId));
	}

	@Test
	void createNoteDetailResponse_ThreeArgs_UserIsSharedUser_EmptyPermission() {
		Integer createdUserId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(false)
				.sharedUsers(List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();

		assertEquals(expected, target.createNoteDetailResponse(note, List.of(), 2));
	}

	@Test
	void createNoteDetailResponse_ThreeArgs_UserIsSharedUser_WithPermission() {
		Integer createdUserId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		UserPermission userPermission = UserPermission.builder()
				.id(100)
				.user(User.builder().id(2).build())
				.type("{\"readOnly\": false, \"readWrite\": true}")
				.build();
		UserAuthorization userAuthorization = UserAuthorization.builder()
				.permissionId(userPermission.getId())
				.userId(userPermission.getUser().getId())
				.type(userPermission.toPermissionType())
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(false)
				.sharedUsers(List.of(userAuthorization))
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();

		assertEquals(expected, target.createNoteDetailResponse(note, List.of(userPermission), 2));
	}

	@Test
	void createNoteDetailResponse_TwoArgs_UserIsAuthor() {
		Integer createdUserId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(true)
				.sharedUsers(List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();

		assertEquals(expected, target.createNoteDetailResponse(note, createdUserId));
	}

	@Test
	void createNoteDetailResponse_TwoArgs_UserIsSharedUser() {
		Integer createdUserId = 1;
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(createdUserId).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(false)
				.sharedUsers(List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(note.isDeletedFlag())
				.build();

		assertEquals(expected, target.createNoteDetailResponse(note, 2));
	}

	@Test
	void createNote_ReturnNote() {
		User user = User.builder().id(1).name("tester").build();
		Note expected = Note.builder()
				.title("")
				.contents("")
				.createdUser(user)
				.updatedUser(user)
				.build();

		assertEquals(expected, target.createNote(user));
	}

	@Test
	void updateNote_ReturnNote() {
		Integer noteId = 100;
		User user = User.builder().id(1).name("tester").build();
		NoteUpdateRequest noteUpdateRequest = NoteUpdateRequest.builder()
				.title("Test title")
				.contents("updateNote test")
				.build();
		Note expected = Note.builder()
				.id(noteId)
				.title(noteUpdateRequest.getTitle())
				.contents(noteUpdateRequest.getContents())
				.updatedUser(user)
				.build();

		assertEquals(expected, target.updateNote(noteId, noteUpdateRequest, user));
	}
}
