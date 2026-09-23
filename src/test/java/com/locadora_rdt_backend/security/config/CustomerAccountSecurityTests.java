package com.locadora_rdt_backend.security.config;

import com.locadora_rdt_backend.modules.identity.customer_account.controller.CustomerAccountController;
import com.locadora_rdt_backend.modules.identity.customer_account.service.CustomerAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerAccountController.class)
@ContextConfiguration(classes = {CustomerAccountController.class, ResourceServerConfig.class})
class CustomerAccountSecurityTests {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerAccountService service;

    @MockBean
    private ResourceServerTokenServices tokenServices;

    @Test
    void anonymousCustomerCanRegister() throws Exception {
        mvc.perform(post("/customer-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cliente Teste\",\"cpf\":\"12345678901\","
                        + "\"email\":\"cliente@example.com\",\"phone\":\"11999999999\","
                        + "\"street\":\"Rua Teste\",\"number\":\"1\",\"neighborhood\":\"Centro\","
                        + "\"city\":\"São Paulo\",\"state\":\"SP\",\"zipCode\":\"01001000\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousCustomerCanCreatePasswordWithActivationToken() throws Exception {
        mvc.perform(post("/customer-accounts/create-password").param("token", "activation-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"secret123\",\"passwordConfirmation\":\"secret123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void anonymousCustomerCanResendActivation() throws Exception {
        mvc.perform(post("/customer-accounts/resend-activation")
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"cliente@example.com\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void otherEndpointsAndMethodsStillRequireAuthentication() throws Exception {
        mvc.perform(get("/customers")).andExpect(status().isUnauthorized());
        mvc.perform(get("/customer-accounts")).andExpect(status().isUnauthorized());
        mvc.perform(post("/customer-accounts/other")).andExpect(status().isUnauthorized());
    }
}
