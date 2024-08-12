package app.diy.note_taking_app.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.repository.NoteRepository;
import app.diy.note_taking_app.repository.UserPermissionRepository;
import app.diy.note_taking_app.service.factory.NoteFactory;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

	@InjectMocks
	private NoteServiceImpl target;

	@Mock
	private UserPermissionRepository mockUserPermissionRepository;

	@Mock
	private NoteRepository mockNoteRepository;

	@Mock
	private NoteFactory mockNoteFactory;

	@Mock
	private EntityManager mockEntityManager;

	private static Stream<Arguments> getNoteListPatternProvider() {
		User firstUser = User.builder()
				.id(1)
				.name("First user")
				.deletedFlag(false)
				.build();
		User secondUser = User.builder()
				.id(2)
				.name("Second user")
				.deletedFlag(false)
				.build();
		Note firstNote = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(firstUser)
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(firstUser)
				.build();
		Note secondNote = Note.builder()
				.id(2)
				.title("Title 2")
				.contents("Second note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(firstUser)
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(secondUser)
				.build();
		Note deletedNote = Note.builder()
				.id(2)
				.title("Title Deleted")
				.contents("Deleted note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(firstUser)
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(secondUser)
				.deletedFlag(true)
				.build();
		PreviewNoteResponse firstPreviewNoteResponse = PreviewNoteResponse.builder()
				.id(firstNote.getId())
				.title(firstNote.getTitle())
				.contents(firstNote.getContents())
				.createdAt(firstNote.getCreatedAt())
				.createdBy(firstNote.getCreatedUser().getName())
				.updatedAt(firstNote.getUpdatedAt())
				.updatedBy(firstNote.getUpdatedUser().getName())
				.deletedFlag(false)
				.build();
		PreviewNoteResponse secondPreviewNoteResponse = PreviewNoteResponse.builder()
				.id(secondNote.getId())
				.title(secondNote.getTitle())
				.contents(secondNote.getContents())
				.createdAt(secondNote.getCreatedAt())
				.createdBy(secondNote.getCreatedUser().getName())
				.updatedAt(secondNote.getUpdatedAt())
				.updatedBy(secondNote.getUpdatedUser().getName())
				.build();
		PreviewNoteResponse previewNoteResponseWithDeletedNote = PreviewNoteResponse.builder()
				.id(deletedNote.getId())
				.title(deletedNote.getTitle())
				.contents(deletedNote.getContents())
				.createdAt(deletedNote.getCreatedAt())
				.createdBy(deletedNote.getCreatedUser().getName())
				.updatedAt(deletedNote.getUpdatedAt())
				.updatedBy(deletedNote.getUpdatedUser().getName())
				.deletedFlag(deletedNote.isDeletedFlag())
				.build();

		return Stream.of(
				Arguments.of(
						firstUser.getId(),
						List.of(firstNote),
						List.of(),
						List.of(firstNote),
						List.of(firstPreviewNoteResponse)),
				Arguments.of(
						firstUser.getId(),
						List.of(firstNote),
						List.of(UserPermission.builder()
								.note(Note.builder().deletedFlag(true).build())
								.deletedFlag(false)
								.build()),
						List.of(firstNote),
						List.of(firstPreviewNoteResponse)),
				Arguments.of(
						secondUser.getId(),
						List.of(),
						List.of(UserPermission.builder().note(secondNote).deletedFlag(false).build()),
						List.of(secondNote),
						List.of(secondPreviewNoteResponse)),
				Arguments.of(
						firstUser.getId(),
						List.of(deletedNote),
						List.of(UserPermission.builder().note(secondNote).deletedFlag(false).build()),
						List.of(deletedNote, secondNote),
						List.of(previewNoteResponseWithDeletedNote, secondPreviewNoteResponse)),
				Arguments.of(
						firstUser.getId(),
						List.of(deletedNote),
						List.of(UserPermission.builder()
								.note(Note.builder()
										.deletedFlag(true)
										.build())
								.deletedFlag(false)
								.build()),
						List.of(deletedNote),
						List.of(previewNoteResponseWithDeletedNote)),
				Arguments.of(
						firstUser.getId(),
						List.of(firstNote),
						List.of(UserPermission.builder().note(secondNote).deletedFlag(false).build()),
						List.of(firstNote, secondNote),
						List.of(firstPreviewNoteResponse, secondPreviewNoteResponse)));
	}

	/*
	 * 1. Found notesWrittenByUser but none of notesWrittenByOther
	 * 2. Found notesWrittenByUser and notesWrittenByOther that note is deleted
	 * 3. Found notesWrittenByOther but none of notesWrittenByUser
	 * 4. Found notesWrittenByOther and deleted notesWrittenByUser
	 * 5. Found both deleted notesWrittenByUser and notesWrittenByOther
	 * 6. Found both notesWrittenByOther and notesWrittenByUser
	 */
	@ParameterizedTest
	@MethodSource({ "getNoteListPatternProvider" })
	void getNoteList_GivenNormalToken_ReturnPreviewNoteResponse(
			Integer userId,
			List<Note> notesWrittenByUser,
			List<UserPermission> userPermissions,
			List<Note> allNotes,
			List<PreviewNoteResponse> expected) {
		when(mockNoteRepository.findByCreatedUser_Id(userId)).thenReturn(notesWrittenByUser);
		when(mockUserPermissionRepository.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(userId))
				.thenReturn(userPermissions);
		when(mockNoteFactory.createPreviewNoteResponseList(allNotes, userId)).thenReturn(expected);

		assertArrayEquals(expected.toArray(), target.getNoteList(userId).toArray());
	}

	private static Stream<Arguments> getNoteListEmptyListProvider() {
		return Stream.of(
				Arguments.of(
						List.of(),
						List.of()),
				Arguments.of(
						List.of(),
						List.of(UserPermission.builder()
								.note(Note.builder().deletedFlag(true).build())
								.deletedFlag(false)
								.build())));
	}

	@ParameterizedTest
	@MethodSource({ "getNoteListEmptyListProvider" })
	void getNoteList_GivenNormalToken_ReturnEmptyList(
			List<Note> notesWrittenByUser,
			List<UserPermission> userPermissions) {
		when(mockNoteRepository.findByCreatedUser_Id(anyInt())).thenReturn(notesWrittenByUser);
		when(mockUserPermissionRepository.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);

		assertArrayEquals(List.of().toArray(), target.getNoteList(1).toArray());
	}

	@Test
	void getUndeletedNote_GivenExistedNoteId_ReturnOptionalNote() {
		Optional<Note> returnVal = Optional.ofNullable(Note.builder().id(1).build());
		when(mockNoteRepository.findById(anyInt())).thenReturn(returnVal);

		assertEquals(returnVal.get().getId(), target.getNote(1).get().getId());
	}

	@Test
	void getUndeletedNote_GivenDeletedNoteId_ReturnOptionalEmpty() {
		when(mockNoteRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertEquals(Optional.empty(), target.getNote(1));
	}

	@Test
	void getNoteDetail_UserIsAuthor_ReturnNote() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.deletedFlag(false)
				.build();
		List<UserPermission> userPermissions = List.of();
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
				.deletedFlag(false)
				.build();

		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);
		when(mockNoteFactory.createNoteDetailResponse(note, userPermissions, 1))
				.thenReturn(expected);

		assertEquals(expected, target.getNoteDetail(note, 1));
	}

	@Test
	void getNoteDetail_UserIsAuthorAndFoundUserPermission_ReturnNote() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.deletedFlag(false)
				.build();
		List<UserPermission> userPermissions = List.of(UserPermission.builder()
				.id(1)
				.user(User.builder().id(2).build())
				.type("{\"readOnly\": false, \"readWrite\": true}")
				.build());
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(true)
				.sharedUsers(userPermissions.stream()
						.map(userPermission -> UserAuthorization.builder()
								.permissionId(userPermission.getId())
								.userId(userPermission.getUser().getId())
								.type(userPermission.toPermissionType())
								.build())
						.toList())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(false)
				.build();

		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);
		when(mockNoteFactory.createNoteDetailResponse(note, userPermissions, 1))
				.thenReturn(expected);

		assertEquals(expected, target.getNoteDetail(note, 1));
	}

	@Test
	void getNoteDetail_UserIsSharedUserAndEmptyUserPermissions_ReturnNote() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.deletedFlag(false)
				.build();
		List<UserPermission> userPermissions = List.of();
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
				.deletedFlag(false)
				.build();

		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);
		when(mockNoteFactory.createNoteDetailResponse(note, userPermissions, 2))
				.thenReturn(expected);

		assertEquals(expected, target.getNoteDetail(note, 2));
	}

	@Test
	void getNoteDetail_UserIsSharedUserAndMatchedUserInUserPermissions_ReturnNote() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.deletedFlag(false)
				.build();
		List<UserPermission> userPermissions = List.of(
				UserPermission.builder()
						.id(1)
						.user(User.builder().id(2).build())
						.type("{\"readOnly\": false, \"readWrite\": true}")
						.build(),
				UserPermission.builder()
						.id(1)
						.user(User.builder().id(3).build())
						.type("{\"readOnly\": false, \"readWrite\": true}")
						.build());
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(note.getTitle())
				.contents(note.getContents())
				.userIsAuthor(false)
				.sharedUsers(userPermissions.stream()
						.map(userPermission -> UserAuthorization.builder()
								.permissionId(userPermission.getId())
								.userId(userPermission.getUser().getId())
								.type(userPermission.toPermissionType())
								.build())
						.toList())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(false)
				.build();

		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);
		when(mockNoteFactory.createNoteDetailResponse(note, userPermissions, 2))
				.thenReturn(expected);

		assertEquals(expected, target.getNoteDetail(note, 2));
	}

	@Test
	void create_GivenNormalRequest_ReturnNoteDetailResponse() {
		User user = User.builder().id(1).build();
		Note templateNote = Note.builder()
				.title("")
				.contents("")
				.createdUser(user)
				.updatedUser(user)
				.build();
		Note savedNote = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.deletedFlag(false)
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(savedNote.getId())
				.title(savedNote.getTitle())
				.contents(savedNote.getContents())
				.userIsAuthor(true)
				.sharedUsers(List.of())
				.createdAt(savedNote.getCreatedAt())
				.createdBy(savedNote.getCreatedUser().getName())
				.updatedAt(savedNote.getUpdatedAt())
				.updatedBy(savedNote.getUpdatedUser().getName())
				.deletedFlag(false)
				.build();

		when(mockNoteFactory.createNote(user)).thenReturn(templateNote);
		when(mockNoteRepository.save(templateNote)).thenReturn(savedNote);
		when(mockNoteFactory.createNoteDetailResponse(savedNote, 1)).thenReturn(expected);

		assertEquals(expected, target.create(user));
	}

	@Test
	void create_GivenNormalRequest_ThrowException() {
		when(mockNoteRepository.save(any(Note.class))).thenThrow(new RuntimeException());

		DatabaseTransactionalException e = assertThrows(
				DatabaseTransactionalException.class,
				() -> target.create(User.builder().id(1).build()));
		assertEquals("Failed to save note", e.getMessage());
	}

	@Test
	void update_GivenNormalRequest_ReturnNoteDetailResponse() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.deletedFlag(false)
				.build();
		NoteUpdateRequest request = NoteUpdateRequest.builder()
				.title("Updated Title")
				.contents("Updated note")
				.build();
		User user = User.builder()
				.id(1)
				.build();
		Note updatedNote = Note.builder()
				.id(note.getId())
				.title(request.getTitle())
				.contents(request.getContents())
				.updatedUser(user)
				.build();
		Note savedNote = Note.builder()
				.id(note.getId())
				.title(updatedNote.getTitle())
				.contents(updatedNote.getContents())
				.deletedFlag(note.isDeletedFlag())
				.createdAt(note.getCreatedAt())
				.createdUser(note.getCreatedUser())
				.updatedAt(note.getUpdatedAt())
				.updatedUser(note.getUpdatedUser())
				.deletedFlag(false)
				.build();
		NoteDetailResponse expected = NoteDetailResponse.builder()
				.id(note.getId())
				.title(request.getTitle())
				.contents(request.getContents())
				.userIsAuthor(true)
				.sharedUsers(List.of())
				.createdAt(note.getCreatedAt())
				.createdBy(note.getCreatedUser().getName())
				.updatedAt(note.getUpdatedAt())
				.updatedBy(note.getUpdatedUser().getName())
				.deletedFlag(false)
				.build();

		when(mockNoteFactory.updateNote(note.getId(), request, user)).thenReturn(updatedNote);
		when(mockNoteRepository.saveAndFlush(updatedNote)).thenReturn(savedNote);
		doNothing().when(mockEntityManager).refresh(savedNote);
		when(mockNoteFactory.createNoteDetailResponse(
				savedNote,
				List.of(),
				user.getId()))
				.thenReturn(expected);
		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(List.of());

		assertEquals(expected, target.update(
				note.getId(),
				request,
				user));
	}

	@Test
	void update_GivenNormalRequest_ThrowException() {
		when(mockNoteRepository.saveAndFlush(any(Note.class))).thenThrow(new RuntimeException());

		DatabaseTransactionalException e = assertThrows(
				DatabaseTransactionalException.class,
				() -> target.update(
						1,
						NoteUpdateRequest.builder()
								.title("Title 1")
								.contents("First note")
								.build(),
						User.builder()
								.id(1)
								.build()));
		assertEquals("Failed to update note", e.getMessage());
	}

	@Test
	void delete_GivenNormalRequestAndPermissionsExisted_Successful() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		User user = User.builder().id(1).build();

		doNothing().when(mockNoteRepository).deleteNote(note.getId(), user);
		when(mockUserPermissionRepository.existsByNote(note)).thenReturn(true);
		doNothing().when(mockUserPermissionRepository).deleteUserPermissionsByNote(note);

		target.delete(note, user);
		verify(mockNoteRepository, times(1)).deleteNote(note.getId(), user);
		verify(mockUserPermissionRepository, times(1)).existsByNote(note);
		verify(mockUserPermissionRepository, times(1)).deleteUserPermissionsByNote(note);
	}

	@Test
	void delete_GivenNormalRequestAndPermissionsNotExisted_Successful() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		User user = User.builder().id(1).build();

		doNothing().when(mockNoteRepository).deleteNote(note.getId(), user);
		when(mockUserPermissionRepository.existsByNote(note)).thenReturn(false);

		target.delete(note, user);
		verify(mockNoteRepository, times(1)).deleteNote(note.getId(), user);
		verify(mockUserPermissionRepository, times(1)).existsByNote(note);
		verify(mockUserPermissionRepository, never()).deleteUserPermissionsByNote(note);
	}

	@Test
	void delete_GivenNormalRequest_ThrowException() {
		Note note = Note.builder()
				.id(1)
				.title("Title 1")
				.contents("First note")
				.deletedFlag(false)
				.createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
				.createdUser(User.builder().id(1).name("tester").build())
				.updatedAt(LocalDateTime.of(2024, 1, 2, 9, 0))
				.updatedUser(User.builder().name("tester").build())
				.build();
		User user = User.builder().id(1).build();

		doThrow(new RuntimeException()).when(mockNoteRepository).deleteNote(note.getId(), user);

		DatabaseTransactionalException e = assertThrows(
				DatabaseTransactionalException.class,
				() -> target.delete(note, user));
		assertEquals("Failed to delete note", e.getMessage());
	}
}
