package app.diy.note_taking_app.configuration;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import app.diy.note_taking_app.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException, UserNotFoundException {

		final String authHeader = request.getHeader("Authorization"); // To get authorization from header in request
		final User user;
		final String jwtToken;

		// Checking if JWT token exists
		// Header is empty or header does not contain a toke "Bearer", do nothing
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response); // Delegate next process of Filter in FilterChain
			return;
		}

		// Extracting authorization token
		jwtToken = authHeader.substring(7); // 7 is because "Bearer " is 6 characters, including space
		// Fetch a user by userId from token
		user = userRepository
				.findByIdAndDeletedFlagFalse(Integer.parseInt(jwtService.extractUserId(jwtToken)))
				.orElseThrow(() -> new UserNotFoundException("User was not found"));

		// Checking if the user is not authenticated
		// *authenticated user does not have to proceed the process below*
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			// Checking if the token is still valid
			if (jwtService.isTokenValid(jwtToken, user)) {
				// Update SecurityContextHolder and send the request Dispatcher Servlet
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						user,
						null, // null is because the user is still not authenticated
						user.getAuthorities());

				// Build details out of the request and update(set) SecurityContextHolder
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		// Call next process of Filter in FilterChain
		filterChain.doFilter(request, response);
	}
}