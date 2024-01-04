package app.diy.note_taking_app.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.diy.note_taking_app.domain.dto.request.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.request.RegisterRequest;
import app.diy.note_taking_app.domain.dto.response.AuthenticationResponse;
import app.diy.note_taking_app.service.AuthenticationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService authService;

	@PostMapping("/register")
	public AuthenticationResponse register(
			@RequestBody @Validated RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/authenticate")
	public AuthenticationResponse authenticate(
			@RequestBody AuthenticationRequest request) {
		return authService.authenticate(request);
	}
}
