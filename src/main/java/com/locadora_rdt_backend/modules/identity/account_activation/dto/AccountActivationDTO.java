package com.locadora_rdt_backend.modules.identity.account_activation.dto;

import com.locadora_rdt_backend.modules.identity.account_activation.constants.AccountActivationConstants;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class AccountActivationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = AccountActivationConstants.INVALID_PASSWORD)
    private String password;

    public AccountActivationDTO() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
