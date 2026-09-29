package com.locadora_rdt_backend.modules.organization.employees.validation;

import com.locadora_rdt_backend.common.error.FieldMessage;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.employees.constants.EmployeeConstants;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeUpdateDTO;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeUpdateValidator implements ConstraintValidator<EmployeeUpdateValid, EmployeeUpdateDTO> {

    private final HttpServletRequest request;
    private final EmployeeRepository repository;

    public EmployeeUpdateValidator(HttpServletRequest request, EmployeeRepository repository) {
        this.request = request;
        this.repository = repository;
    }

    @Override
    public void initialize(EmployeeUpdateValid ann) {
    }

    @Override
    public boolean isValid(EmployeeUpdateDTO dto, ConstraintValidatorContext context) {

        @SuppressWarnings("unchecked")
        Map<String, String> uriVars = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        long employeeId = Long.parseLong(uriVars.get("id"));

        List<FieldMessage> list = new ArrayList<>();

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            Employee employeeWithEmail = repository.findByEmail(dto.getEmail());
            if (employeeWithEmail != null && !employeeWithEmail.getId().equals(employeeId)) {
                list.add(new FieldMessage("email", EmployeeConstants.EMAIL_ALREADY_EXISTS));
            }
        }

        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            Employee employeeWithPhone = repository.findByPhone(dto.getPhone());
            if (employeeWithPhone != null && !employeeWithPhone.getId().equals(employeeId)) {
                list.add(new FieldMessage("phone", EmployeeConstants.PHONE_ALREADY_EXISTS));
            }
        }

        if (dto.getEmployeeCode() != null && !dto.getEmployeeCode().trim().isEmpty()) {
            Employee employeeWithEmployeeCode = repository.findByEmployeeCode(dto.getEmployeeCode());
            if (employeeWithEmployeeCode != null && !employeeWithEmployeeCode.getId().equals(employeeId)) {
                list.add(new FieldMessage("employeeCode", EmployeeConstants.EMPLOYEE_CODE_ALREADY_EXISTS));
            }
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
