package app.diy.note_taking_app.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public Optional<UserDetailResponse> getUser(int userId) {
		return userRepository.findById(userId);
	}
}
