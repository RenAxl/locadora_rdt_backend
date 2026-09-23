package com.locadora_rdt_backend.modules.identity.customer_account.dto;

import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class CustomerAccountResendDTO {

    @NotBlank(message = CustomerAccountConstants.EMAIL_REQUIRED)
    @Email(message = CustomerAccountConstants.EMAIL_INVALID)
    private String email;

    public CustomerAccountResendDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
