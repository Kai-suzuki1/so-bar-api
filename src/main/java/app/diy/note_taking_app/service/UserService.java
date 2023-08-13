package app.diy.note_taking_app.service;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;

public interface UserService {
	UserDetailResponse getUser(Integer userId);
}
