package ml.echelon133.microblog.user.validators;

import ml.echelon133.microblog.user.NewUserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, NewUserDto> {

    @Override
    public boolean isValid(NewUserDto newUserDto, ConstraintValidatorContext constraintValidatorContext) {
        if (newUserDto.getPassword() == null || newUserDto.getPassword2() == null) return false;
        boolean isValid = false;
        try {
            isValid = newUserDto.getPassword().equals(newUserDto.getPassword2());
        } catch (NullPointerException ex) {
            // skip, because isValid is still false
        }
        return isValid;
    }

    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
    }
}
