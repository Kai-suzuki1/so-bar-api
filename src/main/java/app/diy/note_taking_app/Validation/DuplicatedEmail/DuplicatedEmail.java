package app.diy.note_taking_app.Validation.DuplicatedEmail;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

@Constraint(validatedBy = DuplicatedEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
@Documented
public @interface DuplicatedEmail {

	String message() default "Entered Email is already Used";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
