package app.diy.note_taking_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(NoteTakingAppConfigProperties.class)
@TestPropertySource("classpath:secrets.properties")
public class JwtServiceTest {

	@SpyBean
	private JwtServiceImpl target;

	@SpyBean
	private NoteTakingAppConfigProperties spyNtaProp;

	@MockBean
	private UserRepository mockUserRepository;

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
	void extractUserId_GivenNormalToken_ReturnSubject() {
		assertEquals("1", target.extractUserId(jwtToken));
	}

	@Test
	void extractClaim_GivenNormalToken_ReturnExpiration() {
		Date expected = Date.from(expiration);

		assertEquals(expected, target.extractClaim(jwtToken, Claims::getExpiration));
	}

	@Test
	void generateToken_GivenUserData_ReturnToken() {
		User user = User.builder().id(1).build();
		when(target.generateToken(user)).thenReturn(jwtToken);

		assertEquals(jwtToken, target.generateToken(user));
	}

	@Test
	void isTokenValid_GivenValidTokenAndUser_ReturnTrue() {
		User user = User.builder()
				.id(1)
				.email("sample11@gmail.com")
				.deletedFlag(false)
				.build();

		when(mockUserRepository.findByEmailAndDeletedFlagFalse(user.getEmail())).thenReturn(Optional.of(user));

		assertTrue(target.isTokenValid(jwtToken, user));
	}

	@Test
	void isTokenValid_FoundDeletedUser_NotFound() {
		User user = User.builder()
				.email("sample11@gmail.com")
				.deletedFlag(true)
				.build();
		final String errorMessage = "User was not found";

		when(mockUserRepository.findByEmailAndDeletedFlagFalse(user.getEmail())).thenReturn(Optional.empty());

		UserNotFoundException e = assertThrows(UserNotFoundException.class, () -> target.isTokenValid(jwtToken, user));
		assertEquals(errorMessage, e.getMessage());
	}

	@ParameterizedTest
	@CsvSource({
			"1, 2",
			"2, 1",
	})
	void isTokenValid_UnmatchedUserId_ReturnFalse(
			String userIdInToken,
			Integer userIdFromDb) {
		String token = Jwts.builder()
				.setClaims(new HashMap<>())
				.setSubject(userIdInToken)
				.setIssuedAt(Date.from(issuedAt))
				.setExpiration(Date.from(expiration))
				.signWith(Keys.hmacShaKeyFor(
						Decoders.BASE64.decode(spyNtaProp.decodeSecretKey())),
						SignatureAlgorithm.HS256)
				.compact();
		User user = User.builder()
				.id(userIdFromDb)
				.email("sample11@gmail.com")
				.deletedFlag(false)
				.build();

		when(mockUserRepository.findByEmailAndDeletedFlagFalse(user.getEmail())).thenReturn(Optional.of(user));

		assertFalse(target.isTokenValid(token, user));
	}
}
