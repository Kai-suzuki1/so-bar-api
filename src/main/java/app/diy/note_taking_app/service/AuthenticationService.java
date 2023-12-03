package app.diy.note_taking_app.service;

import app.diy.note_taking_app.domain.dto.request.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.request.RegisterRequest;
import app.diy.note_taking_app.domain.dto.response.AuthenticationResponse;

public interface AuthenticationService {

	public AuthenticationResponse register(RegisterRequest request);

	public AuthenticationResponse authenticate(AuthenticationRequest request);
}
