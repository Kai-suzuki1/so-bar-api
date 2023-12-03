package app.diy.note_taking_app.validation.duplicatedUsername;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

@Constraint(validatedBy = DuplicatedUsernameValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
@Documented
public @interface DuplicatedUsername {

	String message() default "Entered Username is already Used";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
