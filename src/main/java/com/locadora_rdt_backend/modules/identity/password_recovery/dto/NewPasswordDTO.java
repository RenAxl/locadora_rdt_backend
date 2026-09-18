package com.locadora_rdt_backend.modules.identity.password_recovery.dto;

import com.locadora_rdt_backend.modules.identity.password_recovery.constants.PasswordRecoveryConstants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class NewPasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = PasswordRecoveryConstants.PASSWORD_REQUIRED)
    @Size(min = PasswordRecoveryConstants.PASSWORD_MIN_LENGTH,
            message = PasswordRecoveryConstants.PASSWORD_MIN_LENGTH_MESSAGE)
    private String password;

    public NewPasswordDTO() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

