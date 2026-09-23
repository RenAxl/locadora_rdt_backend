package com.locadora_rdt_backend.modules.identity.customer_account.listener;

import com.locadora_rdt_backend.infrastructure.mail.service.EmailService;
import com.locadora_rdt_backend.infrastructure.mail.template.ActivationEmailTemplate;
import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import com.locadora_rdt_backend.modules.identity.customer_account.event.CustomerAccountActivationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CustomerAccountEmailListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerAccountEmailListener.class);

    private final EmailService emailService;
    private final ActivationEmailTemplate templateService;

    @Value(CustomerAccountConstants.FRONTEND_BASE_URL_PROPERTY)
    private String frontendBaseUrl;

    @Value(CustomerAccountConstants.TOKEN_MINUTES_PROPERTY)
    private long tokenMinutes;

    public CustomerAccountEmailListener(
            EmailService emailService,
            ActivationEmailTemplate templateService
    ) {
        this.emailService = emailService;
        this.templateService = templateService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendActivationEmail(CustomerAccountActivationEvent event) {

        try {

            String link = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                    .path(CustomerAccountConstants.CREATE_PASSWORD_PATH)
                    .queryParam(CustomerAccountConstants.TOKEN_QUERY_PARAM, event.getToken())
                    .toUriString();

            String html = templateService.buildTemplate(event.getName(), link, tokenMinutes);

            emailService.sendHtmlEmail(
                    event.getEmail(),
                    CustomerAccountConstants.CREATE_PASSWORD_EMAIL_SUBJECT,
                    html
            );

        } catch (RuntimeException e) {

            LOGGER.error(CustomerAccountConstants.ACTIVATION_EMAIL_SEND_ERROR, event.getEmail(), e);
        }
    }
}
