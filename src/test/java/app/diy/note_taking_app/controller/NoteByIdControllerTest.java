package app.diy.note_taking_app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.diy.note_taking_app.Util.StringUtil;
import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;
import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.dto.ApiError;
import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.request.NoteUpdateRequest;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.entity.Note;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.exceptions.NoteNotFoundException;
import app.diy.note_taking_app.service.NoteService;
import app.diy.note_taking_app.service.UserPermissionService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class NoteByIdControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NoteTakingAppConfigProperties ntaProp;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NoteService mockNoteService;

	@MockBean
	private UserPermissionService mockUserPermissionService;

	private Note note;
	private User accessUser;
	private NoteDetailResponse noteDetailResponse;
	private NoteUpdateRequest noteUpdateRequest;
	private MockedStatic<LocalDateTime> mockTime;
	private LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 9, 0); // "2024/01/01 09:00"
	private String JwtToken;

	@BeforeEach
	void init() {
		mockTime = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
		mockTime.when(LocalDateTime::now).thenReturn(fixedTime);

		JwtToken = Jwts.builder()
				.setClaims(new HashMap<>())
				.setSubject("1")
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() * 1000 * 60))
				.signWith(Keys.hmacShaKeyFor(
						Decoders.BASE64.decode(ntaProp.decodeSecretKey())),
						SignatureAlgorithm.HS256)
				.compact();

		accessUser = User.builder()
				.id(1)
				.name("tester")
				.email("test@gmail.com")
				.role(Role.USER)
				.deletedFlag(false)
				.build();

		note = Note.builder()
				.id(1)
				.title("Title")
				.contents("Contents")
				.deletedFlag(false)
				.createdUser(accessUser)
				.updatedUser(User.builder()
						.id(2)
						.name("sampler")
						.email("sample@gmail.com")
						.deletedFlag(false)
						.build())
				.build();

		// values are defined in case where the user is author
		noteDetailResponse = NoteDetailResponse.builder()
				.id(1)
				.title("Title")
				.contents("Contents")
				.userIsAuthor(true)
				.sharedUsers(List.of(
						UserAuthorization.builder()
								.userId(accessUser.getId())
								.build()))
				.createdAt(LocalDateTime.now())
				.createdBy("tester")
				.updatedAt(LocalDateTime.now())
				.updatedBy("tester")
				.build();

		noteUpdateRequest = NoteUpdateRequest.builder()
				.title("Title")
				.contents("Contents")
				.build();
	}

	@AfterEach
	void cleanUpEach() {
		mockTime.close();
	}

	@Test
	void getNoteDetail_UserIsAuthorAndGivenNormalRequest_Successful() throws Exception {
		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockNoteService.getNoteDetail(any(), any())).thenReturn(noteDetailResponse);

		mockMvc.perform(
				get("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(noteDetailResponse, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).getNoteDetail(any(), any());
	}

	@Test
	void getNoteDetail_UserIsSharedUserAndGivenNormalRequest_Successful() throws Exception {
		noteDetailResponse.setUserIsAuthor(false);
		// noteDetailResponse.setSharedUsers();

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockNoteService.getNoteDetail(any(), any())).thenReturn(noteDetailResponse);

		mockMvc.perform(
				get("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(noteDetailResponse, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).getNoteDetail(any(), any());
	}

	@Test
	void getNoteDetail_NonExistentNote_NotFound() throws Exception {
		when(mockNoteService.getUndeletedNote(any())).thenThrow(new NoteNotFoundException(anyString()));

		mockMvc.perform(
				get("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().isNotFound())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes/1")
								.message("")
								.statusCode(HttpStatus.NOT_FOUND.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();

		verify(mockNoteService, times(0)).getNoteDetail(any(), any());
	}

	static Stream<List<UserAuthorization>> unsharedUserProvider() {
		return Stream.of(
				List.of(),
				List.of(
						UserAuthorization.builder()
								.userId(2)
								.build(),
						UserAuthorization.builder()
								.userId(3)
								.build()));
	}

	@ParameterizedTest
	@MethodSource({ "unsharedUserProvider" })
	void getNoteDetail_UserIsUnsharedUser_Forbidden(List<UserAuthorization> sharedUsers) throws Exception {
		noteDetailResponse.setUserIsAuthor(false);
		noteDetailResponse.setSharedUsers(sharedUsers);

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockNoteService.getNoteDetail(any(), any())).thenReturn(noteDetailResponse);

		mockMvc.perform(
				get("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().isForbidden())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes/1")
								.message("Not allowed to access this note")
								.statusCode(HttpStatus.FORBIDDEN.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}

	@Test
	void updateNote_UserIsAuthorAndGivenNormalRequest_Successful() throws Exception {
		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockNoteService.update(any(), any(), any())).thenReturn(noteDetailResponse);

		mockMvc.perform(
				patch("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(noteUpdateRequest, objectMapper)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(noteDetailResponse, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).update(any(), any(), any());
	}

	@Test
	void updateNote_UserIsSharedUserAndGivenNormalRequest_Successful() throws Exception {
		note.setCreatedUser(User.builder().id(2).build());

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockUserPermissionService.canUpdateNote(note.getId(), accessUser.getId())).thenReturn(true);
		when(mockNoteService.update(any(), any(), any())).thenReturn(noteDetailResponse);

		mockMvc.perform(
				patch("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(noteUpdateRequest, objectMapper)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(noteDetailResponse, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).update(any(), any(), any());
	}

	@Test
	void updateNote_InsufficientAuthorization_Forbidden() throws Exception {
		note.setCreatedUser(User.builder().id(2).build());

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockUserPermissionService.canUpdateNote(note.getId(), accessUser.getId())).thenReturn(false);

		mockMvc.perform(
				patch("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(noteUpdateRequest, objectMapper)))
				.andExpect(status().isForbidden())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes/1")
								.message("Not allowed to update this note")
								.statusCode(HttpStatus.FORBIDDEN.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}

	@Test
	void updateNote_DatabaseTransactionalException_InternalServerError() throws Exception {
		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockUserPermissionService.canUpdateNote(note.getId(), accessUser.getId())).thenReturn(true);
		when(mockNoteService.update(any(), any(), any())).thenThrow(new DatabaseTransactionalException(""));

		mockMvc.perform(
				patch("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(noteUpdateRequest, objectMapper)))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes/1")
								.message("")
								.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}

	@Test
	void updateNote_MaxLength_BadRequest() throws Exception {
		noteUpdateRequest.setTitle(RandomStringUtils.random(256, true, true));
		noteUpdateRequest.setContents(RandomStringUtils.random(65536, true, true));

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);

		mockMvc.perform(
				patch("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(noteUpdateRequest, objectMapper)))
				.andExpect(status().isBadRequest())
				.andExpect(content().json(StringUtil.readFile("updateNote_max_length_error_response.json")))
				.andReturn();
	}

	@Test
	void updateNote_BelowMaxLength_Successful() throws Exception {
		// to avoid UnfinishedStubbingException due to the nested mock, use a variable
		NoteDetailResponse returnVal = objectMapper.readValue(StringUtil.readFile(
				"updateNote_max_length_success_response.json"),
				NoteDetailResponse.class);

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockNoteService.update(any(), any(), any())).thenReturn(returnVal);

		mockMvc.perform(
				patch("/v1/notes/1")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.readFile("updateNote_max_length_success_request.json")))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.readFile("updateNote_max_length_success_response.json")))
				.andReturn();
	}

	@Test
	void deleteNote_UserIsAuthorAndGivenNormalRequest_Successful() throws Exception {
		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		doNothing().when(mockNoteService).delete(note, accessUser);

		mockMvc.perform(
				patch("/v1/notes/1/delete")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andReturn();

		verify(mockNoteService, times(1)).delete(note, accessUser);
	}

	@Test
	void deleteNote_UserIsSharedUserAndGivenNormalRequest_Successful() throws Exception {
		note.setCreatedUser(User.builder().id(2).build());

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockUserPermissionService.canUpdateNote(note.getId(), accessUser.getId())).thenReturn(true);
		doNothing().when(mockNoteService).delete(note, accessUser);

		mockMvc.perform(
				patch("/v1/notes/1/delete")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andReturn();

		verify(mockNoteService, times(1)).delete(note, accessUser);
	}

	@Test
	void deleteNote_InsufficientAuthorization_Forbidden() throws Exception {
		note.setCreatedUser(User.builder().id(2).build());

		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockUserPermissionService.canUpdateNote(note.getId(), accessUser.getId())).thenReturn(false);

		mockMvc.perform(
				patch("/v1/notes/1/delete")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser))
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(noteUpdateRequest, objectMapper)))
				.andExpect(status().isForbidden())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes/1/delete")
								.message("Not allowed to update this note")
								.statusCode(HttpStatus.FORBIDDEN.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}

	@Test
	void deleteNote_DatabaseTransactionalException_InternalServerError() throws Exception {
		when(mockNoteService.getUndeletedNote(any())).thenReturn(note);
		when(mockUserPermissionService.canUpdateNote(note.getId(), accessUser.getId())).thenReturn(true);
		doThrow(new DatabaseTransactionalException("")).when(mockNoteService).delete(note, accessUser);

		mockMvc.perform(
				patch("/v1/notes/1/delete")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes/1/delete")
								.message("")
								.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}

}
