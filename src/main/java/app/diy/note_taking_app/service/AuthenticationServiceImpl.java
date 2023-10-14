package app.diy.note_taking_app.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import app.diy.note_taking_app.constant.Role;
import app.diy.note_taking_app.domain.dto.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.AuthenticationResponse;
import app.diy.note_taking_app.domain.dto.RegisterRequest;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	private final AuthenticationManager authenticationManger;

	@Override
	public AuthenticationResponse register(RegisterRequest request) {
		User user = User.builder()
				.name(request.getUsername())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.role(Role.USER)
				.build();
		userRepository.save(user);

		return AuthenticationResponse.builder()
				.token(jwtService.generateToken(user))
				.build();
	}

	@Override
	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		authenticationManger.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()));
		String jwtToken = jwtService.generateToken(
				userRepository
						.findByEmailAndDeletedFlagFalse(request.getEmail())
						.orElseThrow(() -> new UserNotFoundException("User Not Found By Email")));

		return AuthenticationResponse.builder()
				.token(jwtToken)
				.build();
	}
}
