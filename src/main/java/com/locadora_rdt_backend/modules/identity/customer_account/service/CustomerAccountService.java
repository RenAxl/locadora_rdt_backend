package com.locadora_rdt_backend.modules.identity.customer_account.service;

import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountCreatePasswordDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountRegistrationDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountResendDTO;

public interface CustomerAccountService {

    void register(CustomerAccountRegistrationDTO dto);

    void createPassword(String token, CustomerAccountCreatePasswordDTO dto);

    void resendActivation(CustomerAccountResendDTO dto);

}
