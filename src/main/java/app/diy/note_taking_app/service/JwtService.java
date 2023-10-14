package app.diy.note_taking_app.service;

import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import app.diy.note_taking_app.domain.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

	public String extractUserId(String token);

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

	public String generateToken(User userDetails);

	public String generateToken(Map<String, Object> extraClaims, User userDetails);

	public boolean isTokenValid(String token, UserDetails userDetails);
}
