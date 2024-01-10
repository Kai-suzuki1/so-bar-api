package app.diy.note_taking_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;
import app.diy.note_taking_app.domain.dto.request.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.request.RegisterRequest;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = NoteTakingAppConfigProperties.class)
@TestPropertySource("classpath:secrets.properties")
public class AuthenticationServiceTest {

	@SpyBean
	private AuthenticationServiceImpl target;

	@SpyBean
	private NoteTakingAppConfigProperties spyNtaProp;

	@MockBean
	private UserRepository mockUserRepository;

	@MockBean
	private JwtService mockJwtService;

	@MockBean
	private PasswordEncoder spyPasswordEncoder;

	@MockBean
	private AuthenticationManager spyAuthenticationManager;

	private String jwtToken;
	private MockedStatic<Clock> mockClock;
	private MockedStatic<Instant> mockInstant;
	private Instant issuedAt;
	private Instant expiration;

	@BeforeEach
	void init() {
		Clock clockData = Clock.fixed(Instant.parse("2024-01-01T09:00:00Z"), ZoneId.of("UTC"));
		// Configure the mocked Clock to return the fixed instant
		Instant instant = Instant.now(clockData);

		mockClock = Mockito.mockStatic(Clock.class, CALLS_REAL_METHODS);
		mockInstant = Mockito.mockStatic(Instant.class, CALLS_REAL_METHODS);

		mockClock.when(Clock::systemUTC).thenReturn(clockData);
		mockInstant.when(Instant::now).thenReturn(instant);

		issuedAt = Instant.now();
		expiration = issuedAt.plusSeconds(31536000);

		jwtToken = Jwts.builder()
				.setClaims(new HashMap<>())
				.setSubject("1")
				.setIssuedAt(Date.from(issuedAt))
				.setExpiration(Date.from(expiration))
				.signWith(Keys.hmacShaKeyFor(
						Decoders.BASE64.decode(spyNtaProp.decodeSecretKey())),
						SignatureAlgorithm.HS256)
				.compact();
	}

	@AfterEach
	void cleanUpEach() {
		mockClock.close();
		mockInstant.close();
	}

	@Test
	void register_GivenNormalToken_ReturnAuthenticationResponse() {
		User user = User.builder().id(1).build();
		RegisterRequest request = RegisterRequest.builder()
				.username("tester")
				.email("test@gmail.com")
				.password("test")
				.build();

		when(mockUserRepository.save(any(User.class))).thenReturn(user);
		when(mockJwtService.generateToken(user)).thenReturn(jwtToken);

		assertEquals(jwtToken, target.register(request).getToken());
	}

	@Test
	void register_GivenNormalToken_InternalServerError() {
		RegisterRequest request = RegisterRequest.builder()
				.username("tester")
				.email("test@gmail.com")
				.password("test")
				.build();

		when(mockUserRepository.save(any(User.class))).thenThrow(new RuntimeException());

		DatabaseTransactionalException e = assertThrows(
				DatabaseTransactionalException.class,
				() -> target.register(request).getToken());
		assertEquals("Failed to register user", e.getMessage());
	}

	@Test
	void authenticate_GivenNormalToken_ReturnAuthenticationResponse() {
		User user = User.builder().id(1).build();
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("test@gmail.com")
				.password("test")
				.build();

		when(mockUserRepository.findByEmailAndDeletedFlagFalse(request.getEmail())).thenReturn(Optional.of(user));
		when(mockJwtService.generateToken(user)).thenReturn(jwtToken);

		assertEquals(jwtToken, target.authenticate(request).getToken());
	}

	@Test
	void authenticate_GivenNormalToken_NotFound() {
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("test@gmail.com")
				.password("test")
				.build();

		when(mockUserRepository.findByEmailAndDeletedFlagFalse(request.getEmail())).thenReturn(Optional.empty());

		UserNotFoundException e = assertThrows(
				UserNotFoundException.class,
				() -> target.authenticate(request).getToken());
		assertEquals("User was not found", e.getMessage());
	}
}
