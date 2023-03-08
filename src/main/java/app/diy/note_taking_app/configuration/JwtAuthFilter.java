package app.diy.note_taking_app.configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.diy.note_taking_app.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization"); // To get authorization from header in request
		final String userEmail;
		final String jwtToken;

		// Checking JWT token exists
		// Header is empty or header does not contain a toke "Bearer", do nothing
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response); // Delegate next process of Filter in FilterChain
			return;
		}

		// Extracting authorization token
		jwtToken = authHeader.substring(7); // 7 is because "Bearer " is 6 characters, including space
		// Extracting username( = email)to fetch the user data from DB
		userEmail = jwtService.extractUserName(jwtToken);

		// Checking username is not null and the user is not authenticated(authenticated
		// user does not have to proceed the process)
		if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// Fetch a user by userEmail
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

			// Checking if the token is still valid
			if (jwtService.isTokenValid(jwtToken, userDetails)) {
				// Update SecurityContextHolder and send the request Dispatcher Servlet
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null, // Null is because the user is still Un-authenticated
						userDetails.getAuthorities());

				// Build details out of request and update(set) SecurityContextHolder
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		// Call next process of Filter in FilterChain
		filterChain.doFilter(request, response);
	}
}