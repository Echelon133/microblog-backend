package ml.echelon133.microblog.report.validators;

import ml.echelon133.microblog.report.Report;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReasonValidator implements ConstraintValidator<ValidReason, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        for (Report.Reason r : Report.Reason.values()) {
            if (r.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
