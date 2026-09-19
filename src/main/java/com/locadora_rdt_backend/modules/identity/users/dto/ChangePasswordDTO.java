package com.locadora_rdt_backend.modules.identity.users.dto;

import com.locadora_rdt_backend.modules.identity.users.constants.UserConstants;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChangePasswordDTO {

    @NotBlank(message = UserConstants.CURRENT_PASSWORD_REQUIRED)
    private String currentPassword;

    @NotBlank(message = UserConstants.NEW_PASSWORD_REQUIRED)
    @Size(min = UserConstants.PASSWORD_MIN_LENGTH, message = UserConstants.NEW_PASSWORD_MIN_LENGTH)
    private String newPassword;

    public ChangePasswordDTO() {
    }

    public ChangePasswordDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

