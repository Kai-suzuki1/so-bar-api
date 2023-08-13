package app.diy.note_taking_app.service.factory;

import org.springframework.stereotype.Component;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDetailFactory {

	public UserDetailResponse create(User user) {
		return UserDetailResponse.builder()
				.id(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.password(user.getPassword())
				.build();
	}
}
