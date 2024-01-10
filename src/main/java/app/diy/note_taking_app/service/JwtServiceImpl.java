package app.diy.note_taking_app.service;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

	private final NoteTakingAppConfigProperties ntaProp;
	private final UserRepository userRepository;

	@Override
	public String extractUserId(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	@Override
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = Jwts
				.parserBuilder()
				.setSigningKey(getSignInKey()) // To create to generate or decode token, it needs signing key
				.build()
				.parseClaimsJws(token) // To parse token
				.getBody();
		return claimsResolver.apply(claims); // extract all claims method
	}

	// generate token from UserDetails without extraClaims
	@Override
	public String generateToken(User userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	@Override
	public String generateToken(Map<String, Object> extraClaims, User userDetails) {
		Clock systemClock = Clock.systemUTC();

		return Jwts
				.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getId().toString())
				.setIssuedAt(Date.from(Instant.now(systemClock)))
				.setExpiration(Date.from(Instant.now(systemClock).plusSeconds(31536000))) // expired 1 year
				.signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	@Override
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String userIdInToken = extractUserId(token);
		final String userIdFromDb = userRepository
				.findByEmailAndDeletedFlagFalse(userDetails.getUsername())
				.orElseThrow(() -> new UserNotFoundException("User was not found"))
				.getId()
				.toString();

		return userIdInToken.equals(userIdFromDb) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(Date.from(Instant.now(Clock.systemUTC())));
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(ntaProp.decodeSecretKey());
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
