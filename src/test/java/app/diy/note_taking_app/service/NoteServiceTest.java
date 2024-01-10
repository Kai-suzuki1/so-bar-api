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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.entity.UserPermission;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.exceptions.NoteNotFoundException;
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

	@Spy
	private NoteFactory spyNoteFactory;

	@Mock
	private EntityManager mockEntityManager;

	static Stream<Arguments> unsharedUserProvider() {
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
		PreviewNoteResponse firstPreviewNoteResponse = PreviewNoteResponse.builder()
				.id(firstNote.getId())
				.title(firstNote.getTitle())
				.previewContents(firstNote.getContents())
				.createdAt(firstNote.getCreatedAt())
				.createdBy(firstNote.getCreatedUser().getName())
				.updatedAt(firstNote.getUpdatedAt())
				.updatedBy(firstNote.getUpdatedUser().getName())
				.build();
		PreviewNoteResponse secondPreviewNoteResponse = PreviewNoteResponse.builder()
				.id(secondNote.getId())
				.title(secondNote.getTitle())
				.previewContents(secondNote.getContents())
				.createdAt(secondNote.getCreatedAt())
				.createdBy(secondNote.getCreatedUser().getName())
				.updatedAt(secondNote.getUpdatedAt())
				.updatedBy(secondNote.getUpdatedUser().getName())
				.build();

		return Stream.of(
				Arguments.of(
						List.of(firstNote),
						List.of(),
						List.of(firstPreviewNoteResponse)),
				Arguments.of(
						List.of(firstNote),
						List.of(UserPermission.builder()
								.note(Note.builder().deletedFlag(true).build())
								.deletedFlag(true)
								.build()),
						List.of(firstPreviewNoteResponse)),
				Arguments.of(
						List.of(),
						List.of(UserPermission.builder().note(secondNote).deletedFlag(false).build()),
						List.of(secondPreviewNoteResponse)),
				Arguments.of(
						List.of(firstNote),
						List.of(UserPermission.builder().note(secondNote).deletedFlag(false).build()),
						List.of(firstPreviewNoteResponse, secondPreviewNoteResponse)),
				Arguments.of(
						List.of(),
						List.of(),
						List.of()));
	}

	/*
	 * 1. Found notesWrittenByUser but notesWrittenByOther
	 * 2. Found notesWrittenByUser and notesWrittenByOther that note is deleted
	 * 3. Found notesWrittenByOther but notesWrittenByUser
	 * 4. Found notesWrittenByOther and notesWrittenByUser
	 * 5. Found none of notesWrittenByOther and notesWrittenByUser
	 */
	@ParameterizedTest
	@MethodSource({ "unsharedUserProvider" })
	void getNoteList_GivenNormalToken_ReturnSubject(
			List<Note> notes,
			List<UserPermission> userPermissions,
			List<PreviewNoteResponse> expected) {
		when(mockNoteRepository.findByCreatedUser_IdAndDeletedFlagFalse(anyInt())).thenReturn(notes);
		when(mockUserPermissionRepository.findByUser_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);

		assertArrayEquals(expected.toArray(), target.getNoteList(1).toArray());
	}

	@Test
	void getUndeletedNote_GivenExistedNoteId_ReturnNote() {
		Note returnVal = Note.builder().id(1).build();
		when(mockNoteRepository.findByIdAndDeletedFlagFalse(anyInt())).thenReturn(Optional.of(returnVal));

		assertEquals(returnVal.getId(), target.getUndeletedNote(1).getId());
	}

	@Test
	void getUndeletedNote_GivenDeletedNoteId_ReturnNote() {
		when(mockNoteRepository.findByIdAndDeletedFlagFalse(anyInt())).thenReturn(Optional.empty());

		NoteNotFoundException e = assertThrows(
				NoteNotFoundException.class,
				() -> target.getUndeletedNote(1));
		assertEquals("Note was not found", e.getMessage());
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
				.build();

		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(userPermissions);

		assertEquals(expected, target.getNoteDetail(note, 1));
	}

	@Test
	void create_GivenNormalRequest_ReturnNoteDetailResponse() {
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
				.build();

		when(mockNoteRepository.save(any(Note.class))).thenReturn(note);

		assertEquals(expected, target.create(User.builder().id(1).build()));
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
				.build();

		when(mockNoteRepository.saveAndFlush(any(Note.class))).thenReturn(note);
		doNothing().when(mockEntityManager).refresh(note);
		when(mockUserPermissionRepository.findByNote_IdAndDeletedFlagFalseAndAcceptedFlagTrue(anyInt()))
				.thenReturn(List.of());

		assertEquals(expected, target.update(
				note.getId(),
				NoteUpdateRequest.builder()
						.title("Title 1")
						.contents("First note")
						.build(),
				User.builder()
						.id(1)
						.build()));
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
