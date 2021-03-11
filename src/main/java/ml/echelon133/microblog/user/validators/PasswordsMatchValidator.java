package ml.echelon133.microblog.user.validators;

import ml.echelon133.microblog.user.NewUserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, NewUserDto> {

    @Override
    public boolean isValid(NewUserDto newUserDto, ConstraintValidatorContext constraintValidatorContext) {
        if (newUserDto.getPassword() == null) return false;
        return newUserDto.getPassword().equals(newUserDto.getPassword2());
    }

    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
    }
}
