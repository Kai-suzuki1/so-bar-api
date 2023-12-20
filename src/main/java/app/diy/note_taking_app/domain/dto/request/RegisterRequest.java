package app.diy.note_taking_app.domain.dto.request;

import org.hibernate.validator.constraints.Length;

import app.diy.note_taking_app.validation.duplicateEmail.DuplicateEmail;
import app.diy.note_taking_app.validation.duplicateUsername.DuplicateUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "Name can't be empty")
	@Length(max = 120, message = "Name should be 120 words or less")
	@DuplicateUsername
	private String username;

	@NotBlank(message = "Email can't be empty")
	@Pattern(regexp = "^[a-zA-Z0-9_.+-]+@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$", message = "Input email is invalid format")
	@DuplicateEmail
	private String email;

	@NotBlank(message = "Password can't be empty")
	private String password;
}
