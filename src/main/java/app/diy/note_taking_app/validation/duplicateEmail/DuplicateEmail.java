package app.diy.note_taking_app.validation.duplicateEmail;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = DuplicateEmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DuplicateEmail {

	String message() default "Entered Email is already Used";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
