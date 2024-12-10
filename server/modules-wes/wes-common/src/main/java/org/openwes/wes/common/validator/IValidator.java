package org.openwes.wes.common.validator;

public interface IValidator<T, R> {

    R validate(T validateObject);

    ValidatorName getValidatorName();

    enum ValidatorName {
        WAREHOUSE,
        OWNER,
        SKU,
        CONTAINER,
        CONTAINER_SPEC,
        EMPTY_CONTAINER;
    }

}
