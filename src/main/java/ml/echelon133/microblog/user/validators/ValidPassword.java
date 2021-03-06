package ml.echelon133.microblog.user.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy= PasswordValidator.class)
public @interface ValidPassword {
    String message() default "Password doesn't satisfy complexity requirements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
