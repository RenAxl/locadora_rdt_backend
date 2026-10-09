package com.locadora_rdt_backend.modules.reports.financial_reports.service;

import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.locadora_rdt_backend.modules.reports.financial_reports.constants.FinancialReportConstants;
import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportDTO;
import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportFileDTO;
import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.financial_reports.mapper.FinancialReportMapper;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReport;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReportMonth;
import com.locadora_rdt_backend.modules.reports.financial_reports.repository.FinancialReportPayableRepository;
import com.locadora_rdt_backend.modules.reports.financial_reports.repository.FinancialReportReceivableRepository;
import com.locadora_rdt_backend.shared.reports.generator.JasperReportGenerator;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FinancialReportServiceTests {

    @Mock
    private FinancialReportReceivableRepository receivableRepository;

    @Mock
    private FinancialReportPayableRepository payableRepository;

    @Mock
    private FinancialReportMapper mapper;

    @Mock
    private JasperReportGenerator reportGenerator;

    @InjectMocks
    private FinancialReportServiceImpl service;

    private Receivable receivable;
    private Payable payable;
    private FinancialReportFilterDTO filters;
    private FinancialReportDTO financialReportDTO;

    @BeforeEach
    void setUp() {
        receivable = new Receivable();
        receivable.setId(1L);
        receivable.setDescription("Aluguel recebido");
        receivable.setAmount(new BigDecimal("120.50"));
        receivable.setPaid(true);
        receivable.setPaymentDate(LocalDate.of(2026, 1, 15));
        receivable.setCreatedAt(Instant.parse("2026-01-15T10:00:00Z"));

        payable = new Payable();
        payable.setId(2L);
        payable.setDescription("Aluguel pago");
        payable.setAmount(new BigDecimal("40.00"));
        payable.setPaid(true);
        payable.setPaymentDate(LocalDate.of(2026, 2, 1));
        payable.setCreatedAt(Instant.parse("2026-02-01T10:00:00Z"));

        filters = new FinancialReportFilterDTO();
        filters.setSearch(" Aluguel ");
        filters.setYear(2026);
        filters.setCustomerId(3L);
        filters.setSupplierId(4L);
        filters.setEmployeeId(5L);
        filters.setPaymentMethodId(6L);
        filters.setMinimumAmount(BigDecimal.ZERO);
        filters.setMaximumAmount(new BigDecimal("200.00"));

        financialReportDTO = new FinancialReportDTO();
        financialReportDTO.setReceivableTotal(new BigDecimal("120.50"));
        financialReportDTO.setPayableTotal(new BigDecimal("40.00"));
        financialReportDTO.setBalance(new BigDecimal("80.50"));
        financialReportDTO.setYear(2026);
    }

    @Test
    void generateShouldReturnAnnualBalanceFile() {
        filters.setStartDate(LocalDate.of(2026, 5, 1));
        filters.setEndDate(LocalDate.of(2026, 5, 31));
        filters.setStatus("PENDING");
        filters.setPeriodType("DUE_DATE");
        when(receivableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "PAID", "PAYMENT_DATE", 3L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(receivable));
        when(payableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "PAID", "PAYMENT_DATE", 4L, 5L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(payable));
        when(reportGenerator.generateExcel(eq("Balanço Anual 2026"),
                eq(FinancialReportConstants.ANNUAL_BALANCE_COLUMNS), anyList())).thenCallRealMethod();

        FinancialReportFileDTO resultado = service.generate("annual-balance", "XLSX", filters);

        assertNotNull(resultado);
        assertEquals("annual_balance.xlsx", resultado.getFileName());
        assertEquals(FinancialReportConstants.XLSX_CONTENT_TYPE, resultado.getContentType());
        assertEquals('P', resultado.getData()[0]);
        assertEquals('K', resultado.getData()[1]);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, ?>>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportGenerator).generateExcel(eq("Balanço Anual 2026"),
                eq(FinancialReportConstants.ANNUAL_BALANCE_COLUMNS), rowsCaptor.capture());
        List<Map<String, ?>> rows = rowsCaptor.getValue();
        assertEquals(13, rows.size());
        assertEquals("Janeiro", rows.get(0).get("column0"));
        assertEquals("Fevereiro", rows.get(1).get("column0"));
        assertEquals("Total do ano", rows.get(12).get("column0"));
        assertEquals(rows.get(0).get("column1"), rows.get(12).get("column1"));
        assertEquals(rows.get(1).get("column2"), rows.get(12).get("column2"));
        assertEquals("PENDING", filters.getStatus());
        assertEquals(LocalDate.of(2026, 5, 1), filters.getStartDate());
    }

    @Test
    void generateShouldReturnReceivablesPdfFile() throws Exception {
        filters.setStartDate(LocalDate.of(2026, 1, 1));
        filters.setEndDate(LocalDate.of(2026, 12, 31));
        when(receivableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "ALL", "DUE_DATE", 3L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(receivable));

        FinancialReportFileDTO resultado = service.generate("receivables", " PDF ", filters);

        assertNotNull(resultado);
        assertEquals("receivables.pdf", resultado.getFileName());
        assertEquals(FinancialReportConstants.PDF_CONTENT_TYPE, resultado.getContentType());
        PdfReader reader = new PdfReader(resultado.getData());
        try {
            assertEquals(1, reader.getNumberOfPages());
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains(FinancialReportConstants.RECEIVABLES_REPORT_TITLE));
            assertTrue(text.contains("Aluguel recebido"));
            assertTrue(text.contains("15/01/2026"));
            assertTrue(text.contains("120,50"));
            assertTrue(text.contains(FinancialReportConstants.PAID_STATUS_LABEL));
            assertTrue(text.contains(FinancialReportConstants.TOTAL_LABEL));
        } finally {
            reader.close();
        }
        verifyNoInteractions(payableRepository, mapper, reportGenerator);
    }

    @Test
    void generateShouldThrowExceptionWhenReportTypeIsInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generate("invalid", "pdf", filters));

        assertEquals(FinancialReportConstants.INVALID_REPORT_TYPE, exception.getMessage());
        verify(reportGenerator, never()).generateExcel(any(), anyList(), anyList());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void comparisonShouldReturnFinancialReport() {
        filters.setStatus(" paid ");
        filters.setPeriodType(" created-date ");
        when(receivableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "PAID", "CREATED_DATE", 3L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(receivable));
        when(payableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "PAID", "CREATED_DATE", 4L, 5L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.singletonList(payable));
        when(mapper.toDTO(any(FinancialReport.class))).thenReturn(financialReportDTO);

        FinancialReportDTO resultado = service.comparison(filters);

        assertEquals(financialReportDTO, resultado);
        ArgumentCaptor<FinancialReport> reportCaptor = ArgumentCaptor.forClass(FinancialReport.class);
        verify(mapper).toDTO(reportCaptor.capture());
        FinancialReport report = reportCaptor.getValue();
        assertEquals(new BigDecimal("120.50"), report.getReceivableTotal());
        assertEquals(new BigDecimal("40.00"), report.getPayableTotal());
        assertEquals(new BigDecimal("80.50"), report.getBalance());
        assertEquals(1, report.getReceivableCount());
        assertEquals(1, report.getPayableCount());
        assertEquals(2026, report.getYear());
        assertEquals(12, report.getMonths().size());
        assertEquals(1, report.getMonths().get(0).getMonth());
        assertEquals("Jan", report.getMonths().get(0).getLabel());
        assertEquals(new BigDecimal("120.50"), report.getMonths().get(0).getReceivableTotal());
        assertEquals(new BigDecimal("40.00"), report.getMonths().get(1).getPayableTotal());
        assertEquals(BigDecimal.ZERO, report.getMonths().get(11).getReceivableTotal());
    }

    @Test
    void comparisonShouldReturnZeroTotalsWhenThereAreNoEntries() {
        when(receivableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "ALL", "DUE_DATE", 3L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.emptyList());
        when(payableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "ALL", "DUE_DATE", 4L, 5L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenReturn(Collections.emptyList());
        FinancialReportDTO emptyReportDTO = new FinancialReportDTO();
        when(mapper.toDTO(any(FinancialReport.class))).thenReturn(emptyReportDTO);

        FinancialReportDTO resultado = service.comparison(filters);

        assertEquals(emptyReportDTO, resultado);
        ArgumentCaptor<FinancialReport> reportCaptor = ArgumentCaptor.forClass(FinancialReport.class);
        verify(mapper).toDTO(reportCaptor.capture());
        FinancialReport report = reportCaptor.getValue();
        assertEquals(BigDecimal.ZERO, report.getReceivableTotal());
        assertEquals(BigDecimal.ZERO, report.getPayableTotal());
        assertEquals(BigDecimal.ZERO, report.getBalance());
        assertEquals(0, report.getReceivableCount());
        assertEquals(0, report.getPayableCount());
        assertEquals(2026, report.getYear());
        assertEquals(12, report.getMonths().size());
        for (int index = 0; index < report.getMonths().size(); index++) {
            FinancialReportMonth month = report.getMonths().get(index);
            assertEquals(index + 1, month.getMonth());
            assertEquals(BigDecimal.ZERO, month.getReceivableTotal());
            assertEquals(BigDecimal.ZERO, month.getPayableTotal());
        }
    }

    @Test
    void comparisonShouldThrowExceptionWhenRepositoryFails() {
        when(receivableRepository.find("Aluguel", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                true, true, "ALL", "DUE_DATE", 3L, 6L, BigDecimal.ZERO, new BigDecimal("200.00")))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.comparison(filters));

        verify(mapper, never()).toDTO(any());
    }
}
