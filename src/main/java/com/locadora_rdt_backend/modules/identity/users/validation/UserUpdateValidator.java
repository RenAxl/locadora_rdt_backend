package com.locadora_rdt_backend.modules.identity.users.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.locadora_rdt_backend.common.error.FieldMessage;
import com.locadora_rdt_backend.modules.identity.users.dto.UserUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import org.springframework.web.servlet.HandlerMapping;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

    private final HttpServletRequest request;
    private final UserRepository repository;

    public UserUpdateValidator(HttpServletRequest request, UserRepository repository) {
        this.request = request;
        this.repository = repository;
    }

    @Override
    public void initialize(UserUpdateValid ann) {
    }

    @Override
    public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {

        @SuppressWarnings("unchecked")
        var uriVars = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        long userId = Long.parseLong(uriVars.get("id"));

        List<FieldMessage> list = new ArrayList<>();

        User userWithEmail = repository.findByEmail(dto.getEmail());
        if (userWithEmail != null && !userWithEmail.getId().equals(userId)) {
            list.add(new FieldMessage("email", "O E-mail já existe"));
        }

        User userWithTelephone = repository.findByTelephone(dto.getTelephone());
        if (userWithTelephone != null && !userWithTelephone.getId().equals(userId)) {
            list.add(new FieldMessage("telephone", "O Telefone já existe"));
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
