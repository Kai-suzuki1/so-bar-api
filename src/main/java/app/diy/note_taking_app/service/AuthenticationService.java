package app.diy.note_taking_app.service;

import app.diy.note_taking_app.domain.dto.request.AuthenticationRequest;
import app.diy.note_taking_app.domain.dto.request.RegisterRequest;
import app.diy.note_taking_app.domain.dto.response.AuthenticationResponse;

public interface AuthenticationService {

	AuthenticationResponse register(RegisterRequest request);

	AuthenticationResponse authenticate(AuthenticationRequest request);
}
