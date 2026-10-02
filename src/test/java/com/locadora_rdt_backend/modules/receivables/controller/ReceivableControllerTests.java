package com.locadora_rdt_backend.modules.receivables.controller;

import com.locadora_rdt_backend.modules.financial.receivables.controller.ReceivableController;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReceivableControllerTests {

    @Test
    void installmentEndpointShouldNotBeAvailable() throws Exception {
        ReceivableService service = mock(ReceivableService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReceivableController(service)).build();

        mvc.perform(post("/receivables/1/installments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"installments\":3,\"firstDueDate\":\"2026-10-15\"}"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }
}
