package app.diy.note_taking_app.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public Optional<UserDetailResponse> getUser(Integer userId) {
		Optional<User> user = userRepository.findById(userId);
		return Optional.ofNullable(UserDetailResponse.builder()
				.id(user.get().getId())
				.name(user.get().getName())
				.email(user.get().getEmail())
				.passWord(user.get().getPassword())
				.build());
	}
}
