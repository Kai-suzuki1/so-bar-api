package app.diy.note_taking_app.service.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDetailFactory {

	public UserDetailResponse create(Optional<User> user) {
		return UserDetailResponse.builder()
				.id(user.get().getId())
				.name(user.get().getName())
				.email(user.get().getEmail())
				.passWord(user.get().getPassword())
				.build();
	}
}
