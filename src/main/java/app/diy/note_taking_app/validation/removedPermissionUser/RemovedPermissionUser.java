package app.diy.note_taking_app.validation.removedPermissionUser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = RemovedPermissionUserValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemovedPermissionUser {

	String message() default "Invalid User is Selected";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
