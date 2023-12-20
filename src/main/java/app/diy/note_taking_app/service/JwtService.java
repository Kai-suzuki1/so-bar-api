package app.diy.note_taking_app.service;

import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import app.diy.note_taking_app.domain.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

	String extractUserId(String token);

	<T> T extractClaim(String token, Function<Claims, T> claimsResolver);

	String generateToken(User userDetails);

	String generateToken(Map<String, Object> extraClaims, User userDetails);

	boolean isTokenValid(String token, UserDetails userDetails);
}
