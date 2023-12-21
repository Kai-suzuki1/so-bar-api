package app.diy.note_taking_app.service;

import app.diy.note_taking_app.domain.dto.response.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.User;

public interface UserService {

	UserDetailResponse getUser(Integer userId);

	void delete(User user);
}
