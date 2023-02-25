package app.diy.note_taking_app.service;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import java.util.Optional;

public interface UserService {
	Optional<UserDetailResponse> getUser(final int userId);
}
