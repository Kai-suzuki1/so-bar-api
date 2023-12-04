package app.diy.note_taking_app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.diy.note_taking_app.domain.dto.response.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("v1/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public UserDetailResponse getUser(@AuthenticationPrincipal User user) {
		return userService.getUser(user.getId());
	}

	@PatchMapping("/delete")
	public void deleteUser(@AuthenticationPrincipal User user) {
		userService.delete(user.getId());
	}
}
