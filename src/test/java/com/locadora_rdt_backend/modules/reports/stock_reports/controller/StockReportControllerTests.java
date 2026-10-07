package com.locadora_rdt_backend.modules.reports.stock_reports.controller;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.handler.ResourceExceptionHandler;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFileDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportOptionsDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.service.StockReportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(StockReportControllerTests.TestConfig.class)
class StockReportControllerTests {
    @Autowired private StockReportController controller;
    @Autowired private StockReportService service;
    private MockMvc mvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class TestConfig {
        @Bean StockReportService service() { return mock(StockReportService.class); }
        @Bean StockReportController controller(StockReportService service) { return new StockReportController(service); }
    }

    @BeforeEach
    void setUp() {
        reset(service);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tester", "test-only", Collections.singletonList(new SimpleGrantedAuthority("STOCK_REPORTS_READ"))));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ResourceExceptionHandler()).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateShouldReturnFileHeadersAndBytes() throws Exception {
        StockReportFileDTO file = new StockReportFileDTO();
        file.setFileName("balances.pdf");
        file.setContentType("application/pdf");
        file.setData(new byte[]{1, 2, 3});
        when(service.generate(eq("balances"), eq("pdf"), any())).thenReturn(file);
        mvc.perform(get("/reports/stock-reports/balances/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"balances.pdf\""))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void summaryShouldReturnQuantities() throws Exception {
        StockReportDTO dto = new StockReportDTO();
        dto.setTotalQuantity(5);
        when(service.summary(any())).thenReturn(dto);
        mvc.perform(get("/reports/stock-reports/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalQuantity").value(5));
    }

    @Test
    void optionsShouldReturnEmptyListsWithoutItems() throws Exception {
        when(service.options()).thenReturn(new StockReportOptionsDTO());
        mvc.perform(get("/reports/stock-reports/options").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.categories").isEmpty());
    }

    @Test
    void allEndpointsShouldRejectUsersWithoutReportPermission() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tester", "test-only", Collections.singletonList(new SimpleGrantedAuthority("ITEM_READ"))));
        assertThrows(AccessDeniedException.class, () -> controller.generate("balances", "pdf", new StockReportFilterDTO()));
        assertThrows(AccessDeniedException.class, () -> controller.summary(new StockReportFilterDTO()));
        assertThrows(AccessDeniedException.class, () -> controller.options());
        verifyNoInteractions(service);
    }

    @Test
    void invalidReportShouldReturnBadRequest() throws Exception {
        when(service.generate(eq("invalid"), eq("pdf"), any())).thenThrow(new DatabaseException("Tipo inválido"));
        mvc.perform(get("/reports/stock-reports/invalid/pdf").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Tipo inválido"));
    }

    @Test
    void malformedDateShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/reports/stock-reports/movements/pdf").param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
