package com.locadora_rdt_backend.modules.identity.account_activation.service;

import com.locadora_rdt_backend.modules.identity.account_activation.dto.AccountActivationDTO;
import com.locadora_rdt_backend.modules.identity.users.model.User;

public interface AccountActivationService {

    void createActivationTokenAndSendEmail(User user);

    void activateAccount (String token, AccountActivationDTO dto);

}
