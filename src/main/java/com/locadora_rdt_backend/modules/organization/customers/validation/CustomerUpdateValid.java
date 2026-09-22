package com.locadora_rdt_backend.modules.organization.customers.validation;

import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = CustomerUpdateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)

public @interface CustomerUpdateValid {
    String message() default CustomerConstants.VALIDATION_ERROR;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
