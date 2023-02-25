package app.diy.note_taking_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.service.UserService;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/{userId}")
	public Optional<UserDetailResponse> getUser(@PathVariable("userId") int id) {
		return userService.getUser(id);
	}
}
