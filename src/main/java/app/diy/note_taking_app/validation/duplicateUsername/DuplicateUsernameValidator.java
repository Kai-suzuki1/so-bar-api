package app.diy.note_taking_app.validation.duplicateUsername;

import app.diy.note_taking_app.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DuplicateUsernameValidator implements ConstraintValidator<DuplicateUsername, String> {

	private final UserRepository userRepository;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return !userRepository.existsByNameAndDeletedFlagFalse(value);
	}
}
