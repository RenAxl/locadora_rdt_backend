package com.locadora_rdt_backend.modules.reports.rental_reports.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.reports.rental_reports.constants.RentalReportConstants;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFileDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.mapper.RentalReportMapper;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReport;
import com.locadora_rdt_backend.modules.reports.rental_reports.repository.RentalReportRepository;
import com.locadora_rdt_backend.shared.reports.generator.JasperReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalReportServiceTests {

    @Mock
    private RentalReportRepository repository;

    @Mock
    private RentalReportMapper mapper;

    @Mock
    private JasperReportGenerator reportGenerator;

    @InjectMocks
    private RentalReportServiceImpl service;

    private Rental rental;
    private RentalReportFilterDTO filters;
    private RentalReportDTO rentalReportDTO;

    @BeforeEach
    void setUp() {
        rental = new Rental();
        rental.setId(1L);
        rental.setRentalNumber("LOC-001");
        rental.setStatus("DELIVERED");
        rental.setRegistrationDate(LocalDate.of(2026, 1, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());
        rental.setTotalAmount(new BigDecimal("120.00"));
        rental.setLateFee(new BigDecimal("10.00"));
        rental.setDiscount(new BigDecimal("5.00"));
        rental.setPaid(true);

        filters = new RentalReportFilterDTO();
        filters.setSearch(" LOC ");
        filters.setYear(2026);
        filters.setCustomerId(3L);
        filters.setRentalTypeId(4L);
        filters.setPaymentMethodId(6L);
        filters.setMinimumAmount(BigDecimal.ZERO);
        filters.setMaximumAmount(new BigDecimal("200.00"));

        rentalReportDTO = new RentalReportDTO();
        rentalReportDTO.setRentalTotal(new BigDecimal("125.00"));
        rentalReportDTO.setPaidTotal(new BigDecimal("125.00"));
        rentalReportDTO.setYear(2026);
    }

    @Test
    void generateShouldReturnAnnualSummaryFile() {
        filters.setStartDate(LocalDate.of(2026, 5, 1));
        filters.setEndDate(LocalDate.of(2026, 5, 31));
        when(repository.find("LOC", LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                LocalDate.of(2027, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                true, true, "ALL", "REGISTRATION_DATE", 3L, 4L, 6L,
                BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(rental));
        when(reportGenerator.generateExcel(eq("Resumo Anual de Locações 2026"),
                eq(RentalReportConstants.ANNUAL_COLUMNS), anyList(), eq(true))).thenCallRealMethod();

        RentalReportFileDTO resultado = service.generate("annual-summary", "XLSX", filters);

        assertNotNull(resultado);
        assertEquals("annual_summary.xlsx", resultado.getFileName());
        assertEquals(RentalReportConstants.XLSX_CONTENT_TYPE, resultado.getContentType());
        assertEquals('P', resultado.getData()[0]);
        assertEquals('K', resultado.getData()[1]);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, ?>>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportGenerator).generateExcel(eq("Resumo Anual de Locações 2026"),
                eq(RentalReportConstants.ANNUAL_COLUMNS), rowsCaptor.capture(), eq(true));
        List<Map<String, ?>> rows = rowsCaptor.getValue();
        assertEquals(13, rows.size());
        assertEquals("Janeiro", rows.get(0).get("column0"));
        assertEquals("Total do ano", rows.get(12).get("column0"));
        assertEquals(rows.get(0).get("column1"), rows.get(12).get("column1"));
        assertEquals(rows.get(0).get("column2"), rows.get(12).get("column2"));
        assertEquals(LocalDate.of(2026, 5, 1), filters.getStartDate());
    }

    @Test
    void generateShouldThrowExceptionWhenReportTypeIsInvalid() {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.generate("invalid", "pdf", filters));

        assertEquals(RentalReportConstants.INVALID_REPORT_TYPE, exception.getMessage());
        verify(repository, never()).find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(reportGenerator, never()).generateExcel(any(), anyList(), anyList(), eq(true));
    }

    @Test
    void comparisonShouldReturnRentalReport() {
        filters.setStatus(" delivered ");
        filters.setPeriodType(" registration-date ");
        when(repository.find("LOC", LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                LocalDate.of(2027, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                true, true, "DELIVERED", "REGISTRATION_DATE", 3L, 4L, 6L,
                BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(rental));
        when(mapper.toDTO(any(RentalReport.class))).thenReturn(rentalReportDTO);

        RentalReportDTO resultado = service.comparison(filters);

        assertEquals(rentalReportDTO, resultado);
        ArgumentCaptor<RentalReport> reportCaptor = ArgumentCaptor.forClass(RentalReport.class);
        verify(mapper).toDTO(reportCaptor.capture());
        RentalReport report = reportCaptor.getValue();
        assertEquals(new BigDecimal("125.00"), report.getRentalTotal());
        assertEquals(new BigDecimal("125.00"), report.getPaidTotal());
        assertEquals(1, report.getRentalCount());
        assertEquals(1, report.getPaidCount());
        assertEquals(2026, report.getYear());
        assertEquals(12, report.getMonths().size());
        assertEquals("Jan", report.getMonths().get(0).getLabel());
        assertEquals(new BigDecimal("125.00"), report.getMonths().get(0).getRentalTotal());
        assertEquals(new BigDecimal("125.00"), report.getMonths().get(0).getPaidTotal());
        assertEquals(BigDecimal.ZERO, report.getMonths().get(11).getRentalTotal());
        assertEquals(" delivered ", filters.getStatus());
    }

    @Test
    void comparisonShouldThrowExceptionWhenRepositoryFails() {
        when(repository.find("LOC", LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                LocalDate.of(2027, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                true, true, "ALL", "REGISTRATION_DATE", 3L, 4L, 6L,
                BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.comparison(filters));

        verify(mapper, never()).toDTO(any());
    }
}
