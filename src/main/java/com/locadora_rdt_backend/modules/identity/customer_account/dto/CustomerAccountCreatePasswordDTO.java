package com.locadora_rdt_backend.modules.identity.customer_account.dto;

import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CustomerAccountCreatePasswordDTO {

    @NotBlank(message = CustomerAccountConstants.PASSWORD_REQUIRED)
    @Size(min = CustomerAccountConstants.PASSWORD_MIN_LENGTH,
            message = CustomerAccountConstants.PASSWORD_MIN_LENGTH_MESSAGE)
    private String password;

    @NotBlank(message = CustomerAccountConstants.PASSWORD_CONFIRMATION_REQUIRED)
    private String passwordConfirmation;

    public CustomerAccountCreatePasswordDTO() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

}
