package com.locadora_rdt_backend.modules.organization.employees.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.locadora_rdt_backend.common.error.FieldMessage;
import com.locadora_rdt_backend.modules.organization.employees.constants.EmployeeConstants;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeInsertDTO;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;

public class EmployeeInsertValidator implements ConstraintValidator<EmployeeInsertValid, EmployeeInsertDTO> {

    private final EmployeeRepository repository;

    public EmployeeInsertValidator(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initialize(EmployeeInsertValid ann) {
    }

    @Override
    public boolean isValid(EmployeeInsertDTO dto, ConstraintValidatorContext context) {

        if (dto == null) {
            return true;
        }

        List<FieldMessage> list = new ArrayList<>();

        if (repository.existsByEmail(dto.getEmail())) {
            list.add(new FieldMessage("email", EmployeeConstants.EMAIL_ALREADY_EXISTS));
        }

        if (repository.existsByEmployeeCode(dto.getEmployeeCode())) {
            list.add(new FieldMessage("employeeCode", EmployeeConstants.EMPLOYEE_CODE_ALREADY_EXISTS));
        }

        if (repository.existsByPhone(dto.getPhone())) {
            list.add(new FieldMessage("phone", EmployeeConstants.PHONE_ALREADY_EXISTS));
        }

        if (dto.getHireDate() != null && dto.getTerminationDate() != null
                && dto.getTerminationDate().isBefore(dto.getHireDate())) {
            list.add(new FieldMessage(
                    "terminationDate",
                    EmployeeConstants.TERMINATION_DATE_BEFORE_HIRE_DATE
            ));
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
