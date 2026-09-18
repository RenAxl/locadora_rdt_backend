package com.locadora_rdt_backend.modules.identity.password_recovery.controller;

import com.locadora_rdt_backend.modules.identity.password_recovery.dto.ForgotPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.dto.NewPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.service.PasswordRecoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/auth")
public class PasswordRecoveryController {

    private final PasswordRecoveryService service;

    public PasswordRecoveryController(PasswordRecoveryService service) {
        this.service = service;
    }

    @PostMapping(value = "/request-password-reset")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {

        service.requestPasswordReset(dto);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/password-reset")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @Valid @RequestBody NewPasswordDTO dto) {

        service.resetPassword(token, dto);

        return ResponseEntity.noContent().build();
    }
}
