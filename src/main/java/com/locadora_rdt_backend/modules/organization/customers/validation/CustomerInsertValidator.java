package com.locadora_rdt_backend.modules.organization.customers.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.locadora_rdt_backend.common.error.FieldMessage;
import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerInsertDTO;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;

public class CustomerInsertValidator implements ConstraintValidator<CustomerInsertValid, CustomerInsertDTO> {

    private final CustomerRepository repository;

    public CustomerInsertValidator(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initialize(CustomerInsertValid ann) {
    }

    @Override
    public boolean isValid(CustomerInsertDTO dto, ConstraintValidatorContext context) {

        List<FieldMessage> list = new ArrayList<>();

        if (repository.existsByEmail(dto.getEmail())) {
            list.add(new FieldMessage("email", CustomerConstants.EMAIL_ALREADY_EXISTS));
        }

        if (repository.existsByCpf(dto.getCpf())) {
            list.add(new FieldMessage("cpf", CustomerConstants.CPF_ALREADY_EXISTS));
        }

        if (repository.existsByPhone(dto.getPhone())) {
            list.add(new FieldMessage("phone", CustomerConstants.PHONE_ALREADY_EXISTS));
        }

        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addPropertyNode(e.getFieldName())
                    .addConstraintViolation();
        }

        return list.isEmpty();
    }

}
