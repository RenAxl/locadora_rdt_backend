package com.locadora_rdt_backend.modules.identity.customer_account.controller;

import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountCreatePasswordDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountRegistrationDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountResendDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.service.CustomerAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/customer-accounts")
public class CustomerAccountController {

    private final CustomerAccountService service;

    public CustomerAccountController(CustomerAccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody CustomerAccountRegistrationDTO dto) {
        service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/create-password")
    public ResponseEntity<Void> createPassword(@RequestParam String token,
                                               @Valid @RequestBody CustomerAccountCreatePasswordDTO dto) {
        service.createPassword(token, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/resend-activation")
    public ResponseEntity<Void> resendActivation(@Valid @RequestBody CustomerAccountResendDTO dto) {
        service.resendActivation(dto);
        return ResponseEntity.noContent().build();
    }
}
