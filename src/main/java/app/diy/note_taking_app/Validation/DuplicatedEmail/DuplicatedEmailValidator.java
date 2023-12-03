package app.diy.note_taking_app.validation.duplicatedEmail;

import app.diy.note_taking_app.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DuplicatedEmailValidator implements ConstraintValidator<DuplicatedEmail, String> {

	private final UserRepository userRepository;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return !userRepository.existsByEmailAndDeletedFlagFalse(value);
	}
}
