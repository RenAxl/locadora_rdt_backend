package com.locadora_rdt_backend.modules.identity.password_recovery.dto;

import com.locadora_rdt_backend.modules.identity.password_recovery.constants.PasswordRecoveryConstants;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class ForgotPasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = PasswordRecoveryConstants.EMAIL_REQUIRED)
    @Email(message = PasswordRecoveryConstants.EMAIL_INVALID)
    private String email;

    public ForgotPasswordDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
