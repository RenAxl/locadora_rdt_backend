package com.locadora_rdt_backend.modules.organization.customers.validation;

import com.locadora_rdt_backend.common.error.FieldMessage;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerUpdateDTO;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerUpdateValidator implements ConstraintValidator<CustomerUpdateValid, CustomerUpdateDTO> {

    private final HttpServletRequest request;
    private final CustomerRepository repository;

    public CustomerUpdateValidator(HttpServletRequest request, CustomerRepository repository) {
        this.request = request;
        this.repository = repository;
    }

    @Override
    public void initialize(CustomerUpdateValid ann) {
    }

    @Override
    public boolean isValid(CustomerUpdateDTO dto, ConstraintValidatorContext context) {

        @SuppressWarnings("unchecked")
        var uriVars = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        long customerId = Long.parseLong(uriVars.get("id"));

        List<FieldMessage> list = new ArrayList<>();

        Customer customerWithEmail = repository.findByEmail(dto.getEmail());
        if (customerWithEmail != null && !customerWithEmail.getId().equals(customerId)) {
            list.add(new FieldMessage("email", CustomerConstants.EMAIL_ALREADY_EXISTS));
        }

        Customer customerWithPhone = repository.findByPhone(dto.getPhone());
        if (customerWithPhone != null && !customerWithPhone.getId().equals(customerId)) {
            list.add(new FieldMessage("phone", CustomerConstants.PHONE_ALREADY_EXISTS));
        }

        Customer customerWithCpf = repository.findByCpf(dto.getCpf());
        if (customerWithCpf != null && !customerWithCpf.getId().equals(customerId)) {
            list.add(new FieldMessage("cpf", CustomerConstants.CPF_ALREADY_EXISTS));
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
