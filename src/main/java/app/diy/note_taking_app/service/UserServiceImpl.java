package app.diy.note_taking_app.service;

import org.springframework.stereotype.Service;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.exceptions.UserNotFoundException;
import app.diy.note_taking_app.repository.UserRepository;
import app.diy.note_taking_app.service.factory.UserDetailFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final UserDetailFactory userDetailFactory;

	@Override
	public UserDetailResponse getUser(Integer userId) {
		return userDetailFactory
				.create(userRepository.findById(userId)
						.orElseThrow(() -> new UserNotFoundException("User Not Found")));
	}
}
