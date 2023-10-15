package app.diy.note_taking_app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.User;
import app.diy.note_taking_app.exceptions.MismatchUserDetailException;
import app.diy.note_taking_app.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/{userId}")
	public UserDetailResponse getUser(@PathVariable("userId") Integer id, @AuthenticationPrincipal User user) {
		if (id != user.getId()) {
			throw new MismatchUserDetailException("Not Allowed to Access Different User");
		}

		return userService.getUser(id);
	}
}
