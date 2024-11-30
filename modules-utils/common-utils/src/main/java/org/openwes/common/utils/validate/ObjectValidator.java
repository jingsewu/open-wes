package org.openwes.common.utils.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectValidator implements ConstraintValidator<ValidObject, IValidate> {

    @Override
    public boolean isValid(IValidate validate, ConstraintValidatorContext context) {
        return validate.validate();
    }

}
