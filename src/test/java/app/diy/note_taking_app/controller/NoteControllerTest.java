package app.diy.note_taking_app.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.diy.note_taking_app.Util.StringUtil;
import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;
import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.dto.ApiError;
import app.diy.note_taking_app.domain.dto.UserAuthorization;
import app.diy.note_taking_app.domain.dto.response.NoteDetailResponse;
import app.diy.note_taking_app.domain.dto.response.PreviewNoteResponse;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.service.NoteService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class NoteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NoteTakingAppConfigProperties ntaProp;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NoteService mockNoteService;

	private User accessUser;
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
	}

	@AfterEach
	void cleanUpEach() {
		mockTime.close();
	}

	@Test
	void getNoteList_ReturnListOfPreviewNoteResponse_Successful() throws Exception {
		List<PreviewNoteResponse> previewNoteResponses = List.of(PreviewNoteResponse.builder()
				.id(1)
				.title("Title")
				.contents("Preview Contents")
				.createdAt(LocalDateTime.now())
				.createdBy("tester")
				.updatedAt(LocalDateTime.now())
				.updatedBy("sampler")
				.deletedFlag(false)
				.deletableFlag(true)
				.build());

		when(mockNoteService.getNoteList(accessUser.getId())).thenReturn(previewNoteResponses);

		mockMvc.perform(
				get("/v1/notes")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(previewNoteResponses, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).getNoteList(accessUser.getId());
	}

	@Test
	void getNoteList_ReturnEmptyList_Successful() throws Exception {
		List<PreviewNoteResponse> previewNoteResponses = List.of();

		when(mockNoteService.getNoteList(accessUser.getId())).thenReturn(previewNoteResponses);

		mockMvc.perform(
				get("/v1/notes")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(previewNoteResponses, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).getNoteList(accessUser.getId());
	}

	@Test
	void createNote_ReturnNoteDetailResponse_Successful() throws Exception {
		// values are defined in case where the user is author
		NoteDetailResponse noteDetailResponse = NoteDetailResponse.builder()
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

		when(mockNoteService.create(accessUser)).thenReturn(noteDetailResponse);

		mockMvc.perform(
				post("/v1/notes")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(noteDetailResponse, objectMapper)))
				.andReturn();

		verify(mockNoteService, times(1)).create(accessUser);
	}

	@Test
	void createNote_DatabaseTransactionalException_InternalServerError() throws Exception {
		when(mockNoteService.create(accessUser)).thenThrow(new DatabaseTransactionalException(""));

		mockMvc.perform(
				post("/v1/notes")
						.header("Authorization", JwtToken)
						.with(SecurityMockMvcRequestPostProcessors.user(accessUser)))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/notes")
								.message("")
								.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}
}
