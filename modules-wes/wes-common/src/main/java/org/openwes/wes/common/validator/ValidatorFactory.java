package org.openwes.wes.common.validator;

import org.openwes.common.utils.exception.WmsException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ValidatorFactory {

    private static List<IValidator> validators;

    @Autowired
    public ValidatorFactory(List<IValidator> validators) {
        ValidatorFactory.validators = validators;
    }

    public static List<IValidator> getValidators(Set<IValidator.ValidatorName> validateNames) {

        if (validators == null) {
            return Collections.emptyList();
        }

        if (validateNames == null) {
            return Collections.emptyList();
        }

        return validators.stream().filter(validator -> validateNames.contains(validator.getValidatorName())).toList();
    }

    public static IValidator getValidator(@NotNull IValidator.ValidatorName validatorName) {

        if (validators == null) {
            throw new WmsException("validators is null");
        }

        if (validatorName == null) {
            throw new WmsException("validator name must not be null");
        }

        IValidator iValidator = validators.stream()
            .filter(validator -> validator.getValidatorName() == validatorName).findFirst().orElse(null);

        if (iValidator == null) {
            throw new WmsException("Invalid validator name");
        }

        return iValidator;
    }
}
