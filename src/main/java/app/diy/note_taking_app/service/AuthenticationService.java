package app.diy.note_taking_app.service;

import app.diy.note_taking_app.domain.dto.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.AuthenticationResponse;
import app.diy.note_taking_app.domain.dto.RegisterRequest;

public interface AuthenticationService {

	public AuthenticationResponse register(RegisterRequest request);

	public AuthenticationResponse authenticate(AuthenticationRequest request);
}
