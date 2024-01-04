package app.diy.note_taking_app.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.diy.note_taking_app.Util.StringUtil;
import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;
import app.diy.note_taking_app.domain.dto.ApiError;
import app.diy.note_taking_app.domain.dto.request.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.request.RegisterRequest;
import app.diy.note_taking_app.domain.dto.response.AuthenticationResponse;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import app.diy.note_taking_app.service.AuthenticationService;
import app.diy.note_taking_app.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
// @TestPropertySource("classpath:secrets.properties")
public class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NoteTakingAppConfigProperties ntaProp;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthenticationService mockAuthService;

	@MockBean
	private UserRepository mockUserRepository;

	@MockBean
	private JwtService mockJwtService;

	private AuthenticationResponse authenticationResponse;

	private RegisterRequest registerRequest;

	@BeforeEach
	public void init() {
		authenticationResponse = AuthenticationResponse.builder()
				.token(Jwts.builder()
						.setClaims(new HashMap<>())
						.setSubject("1")
						.setIssuedAt(new Date(System.currentTimeMillis()))
						.setExpiration(new Date(System.currentTimeMillis() * 1000 * 60))
						.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(ntaProp.decodeSecretKey())), SignatureAlgorithm.HS256)
						.compact())
				.build();

		registerRequest = RegisterRequest.builder()
				.username("tester")
				.email("test@gmail.com")
				.password("test")
				.build();
	}

	@Test
	public void register_GivenNormalRequest_Successful() throws Exception {
		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockAuthService.register(any())).thenReturn(authenticationResponse);

		mockMvc.perform(
				post("/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(registerRequest, objectMapper)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(authenticationResponse, objectMapper)))
				.andReturn();

		verify(mockAuthService, times(1)).register(any());
	}

	@Test
	public void register_GivenNormalRequest_InternalServerError() throws Exception {
		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockAuthService.register(any())).thenThrow(new DatabaseTransactionalException(anyString()));

		mockMvc.perform(
				post("/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(registerRequest, objectMapper)))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/auth/register")
								.message("")
								.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}

	@ParameterizedTest
	@ValueSource(strings = { " " })
	@NullAndEmptySource
	public void register_BlankValues_BadRequest(String testVal) throws Exception {
		// change the test data
		registerRequest.setEmail(testVal);
		registerRequest.setUsername(testVal);
		registerRequest.setPassword(testVal);

		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(false);

		try (MockedStatic<LocalDateTime> mock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
			var time = LocalDateTime.of(2024, 1, 1, 9, 0);
			mock.when(LocalDateTime::now).thenReturn(time);

			mockMvc.perform(
					post("/v1/auth/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(StringUtil.convertJsonToString(registerRequest, objectMapper)))
					.andExpect(status().isBadRequest())
					.andExpect(content().json(StringUtil.readFile("register_blank_value_error_responses.json")))
					.andReturn();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void register_MaxLength_BadRequest() throws Exception {
		// change the test data
		registerRequest.setUsername(RandomStringUtils.random(121, true, true));

		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(false);

		try (MockedStatic<LocalDateTime> mock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
			var time = LocalDateTime.of(2024, 1, 1, 9, 0);
			mock.when(LocalDateTime::now).thenReturn(time);

			mockMvc.perform(
					post("/v1/auth/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(StringUtil.convertJsonToString(registerRequest, objectMapper)))
					.andExpect(status().isBadRequest())
					.andExpect(content().json(StringUtil.readFile("register_max_length_error_response.json")))
					.andReturn();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void register_BelowMaxLength_Successful() throws Exception {
		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockAuthService.register(any())).thenReturn(authenticationResponse);

		mockMvc.perform(
				post("/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.readFile("register_max_length_success_request.json")))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(authenticationResponse, objectMapper)))
				.andReturn();
	}

	@ParameterizedTest
	@CsvSource({
			"register_duplicate_email_error_response.json, false, true",
			"register_duplicate_username_error_response.json, true, false",
			"register_duplicate_email_username_error_response.json, true, true",
	})
	public void register_DuplicatePattern_BadRequest(
			String fileName,
			boolean isUsernameDuplicate,
			boolean isEmailDuplicate) throws Exception {
		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(isUsernameDuplicate);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(isEmailDuplicate);

		try (MockedStatic<LocalDateTime> mock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
			var time = LocalDateTime.of(2024, 1, 1, 9, 0);
			mock.when(LocalDateTime::now).thenReturn(time);

			mockMvc.perform(
					post("/v1/auth/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(StringUtil.convertJsonToString(registerRequest, objectMapper)))
					.andExpect(status().isBadRequest())
					.andExpect(content().json(StringUtil.readFile(fileName)))
					.andReturn();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"invalid@email",
			"missing_at_symbol",
			"no_domain@",
			"invalid_domain.com",
			"missing_dot_com"
	})
	public void register_EmailInvalidPattern_BadRequest(String emailVal) throws Exception {
		// change the test data
		registerRequest.setEmail(emailVal);

		when(mockUserRepository.existsByNameAndDeletedFlagFalse(any())).thenReturn(false);
		when(mockUserRepository.existsByEmailAndDeletedFlagFalse(any())).thenReturn(false);

		try (MockedStatic<LocalDateTime> mock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
			var time = LocalDateTime.of(2024, 1, 1, 9, 0);
			mock.when(LocalDateTime::now).thenReturn(time);

			mockMvc.perform(
					post("/v1/auth/register")
							.contentType(MediaType.APPLICATION_JSON)
							.content(StringUtil.convertJsonToString(registerRequest, objectMapper)))
					.andExpect(status().isBadRequest())
					.andExpect(content().json(StringUtil.readFile("register_invalid_pattern_error_response.json")))
					.andReturn();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void authenticate_GivenNormalRequest_Successful() throws Exception {
		when(mockAuthService.authenticate(any())).thenReturn(authenticationResponse);

		mockMvc.perform(
				post("/v1/auth/authenticate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(
								AuthenticationRequest.builder()
										.email("test@gmail.com")
										.password("test")
										.build(),
								objectMapper)))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().json(StringUtil.convertJsonToString(authenticationResponse, objectMapper)))
				.andReturn();

		verify(mockAuthService, times(1)).authenticate(any());
	}

	@Test
	public void authenticate_GivenNormalRequest_NotFound() throws Exception {
		when(mockAuthService.authenticate(any())).thenThrow(new UserNotFoundException(anyString()));

		mockMvc.perform(
				post("/v1/auth/authenticate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(StringUtil.convertJsonToString(
								AuthenticationRequest.builder()
										.email("test@gmail.com")
										.password("test")
										.build(),
								objectMapper)))
				.andExpect(status().isNotFound())
				.andExpect(content().json(StringUtil.convertJsonToString(
						ApiError.builder()
								.path("/v1/auth/authenticate")
								.message("")
								.statusCode(HttpStatus.NOT_FOUND.value())
								.localDateTime(LocalDateTime.now())
								.build(),
						objectMapper)))
				.andReturn();
	}
}
