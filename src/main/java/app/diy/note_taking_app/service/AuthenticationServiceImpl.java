package app.diy.note_taking_app.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.dto.request.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.request.RegisterRequest;
import app.diy.note_taking_app.domain.dto.response.AuthenticationResponse;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.DatabaseTransactionalException;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	private final AuthenticationManager authenticationManager;

	@Override
	public AuthenticationResponse register(RegisterRequest request) {
		try {
			return AuthenticationResponse.builder()
					.token(jwtService.generateToken(userRepository.save(User.builder()
							.name(request.getUsername())
							.email(request.getEmail())
							.password(passwordEncoder.encode(request.getPassword()))
							.role(Role.USER)
							.build())))
					.build();
		} catch (Exception e) {
			throw new DatabaseTransactionalException("Failed to register user", e);
		}
	}

	@Override
	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()));
		String jwtToken = jwtService.generateToken(userRepository
				.findByEmailAndDeletedFlagFalse(request.getEmail())
				.orElseThrow(() -> new UserNotFoundException("User was not found")));

		return AuthenticationResponse.builder().token(jwtToken).build();
	}
}
