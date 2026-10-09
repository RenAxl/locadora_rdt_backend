package com.locadora_rdt_backend.modules.reports.rental_reports.controller;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.handler.ResourceExceptionHandler;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFileDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.service.RentalReportService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(RentalReportControllerTests.TestConfig.class)
class RentalReportControllerTests {

    @Autowired
    private RentalReportController controller;

    @Autowired
    private RentalReportService service;

    private MockMvc mvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class TestConfig {
        @Bean
        RentalReportService service() {
            return mock(RentalReportService.class);
        }

        @Bean
        RentalReportController controller(RentalReportService service) {
            return new RentalReportController(service);
        }
    }

    @BeforeEach
    void setUp() {
        reset(service);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tester", "test-only", Collections.singletonList(new SimpleGrantedAuthority("RENTAL_REPORTS_READ"))));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ResourceExceptionHandler()).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateShouldReturnFileAndBindFilters() throws Exception {
        RentalReportFileDTO file = new RentalReportFileDTO();
        file.setFileName("rentals.pdf");
        file.setContentType("application/pdf");
        file.setData(new byte[]{1, 2, 3});
        when(service.generate(eq("rentals"), eq("pdf"), any())).thenAnswer(invocation -> {
            RentalReportFilterDTO filters = invocation.getArgument(2);
            assertEquals(LocalDate.of(2026, 7, 1), filters.getStartDate());
            assertEquals(2L, filters.getRentalTypeId());
            assertEquals(BigDecimal.ZERO, filters.getMinimumAmount());
            assertEquals("EFFECTIVE_RETURN_DATE", filters.getPeriodType());
            return file;
        });

        mvc.perform(get("/reports/rental-reports/rentals/pdf")
                        .param("startDate", "2026-07-01")
                        .param("rentalTypeId", "2")
                        .param("minimumAmount", "0")
                        .param("periodType", "EFFECTIVE_RETURN_DATE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"rentals.pdf\""))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void comparisonShouldReturnRentalValues() throws Exception {
        RentalReportDTO dto = new RentalReportDTO();
        dto.setRentalTotal(new BigDecimal("125.00"));
        dto.setRentalCount(1);
        when(service.comparison(any())).thenReturn(dto);

        mvc.perform(get("/reports/rental-reports/comparison").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalTotal").value(125))
                .andExpect(jsonPath("$.rentalCount").value(1));
    }

    @Test
    void generateShouldRejectUserWithoutReportPermission() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tester", "test-only", Collections.singletonList(new SimpleGrantedAuthority("RENTAL_READ"))));

        assertThrows(AccessDeniedException.class,
                () -> controller.generate("rentals", "pdf", new RentalReportFilterDTO()));

        verifyNoInteractions(service);
    }

    @Test
    void comparisonShouldRejectUserWithoutReportPermission() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tester", "test-only", Collections.singletonList(new SimpleGrantedAuthority("RENTAL_READ"))));

        assertThrows(AccessDeniedException.class, () -> controller.comparison(new RentalReportFilterDTO()));

        verifyNoInteractions(service);
    }

    @Test
    void generateShouldReturnBadRequestWhenFiltersAreInvalid() throws Exception {
        when(service.generate(eq("rentals"), eq("pdf"), any()))
                .thenThrow(new DatabaseException("Tipo de período inválido."));

        mvc.perform(get("/reports/rental-reports/rentals/pdf")
                        .accept(MediaType.APPLICATION_JSON).param("periodType", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de período inválido."));
    }

    @Test
    void generateShouldReturnBadRequestWhenDateIsMalformed() throws Exception {
        mvc.perform(get("/reports/rental-reports/rentals/pdf").param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
