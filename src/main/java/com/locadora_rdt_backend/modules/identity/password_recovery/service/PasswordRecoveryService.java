package com.locadora_rdt_backend.modules.identity.password_recovery.service;

import com.locadora_rdt_backend.modules.identity.password_recovery.dto.ForgotPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.dto.NewPasswordDTO;

public interface PasswordRecoveryService {

    void requestPasswordReset(ForgotPasswordDTO dto);

    void resetPassword(String token, NewPasswordDTO dto);

}
