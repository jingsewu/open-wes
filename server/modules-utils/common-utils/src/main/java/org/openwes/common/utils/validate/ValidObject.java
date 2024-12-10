package org.openwes.common.utils.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotNull
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ObjectValidator.class})
public @interface ValidObject {

    String message() default "parameter is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
