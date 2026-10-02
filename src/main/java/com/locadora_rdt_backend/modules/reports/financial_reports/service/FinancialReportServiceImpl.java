package com.locadora_rdt_backend.modules.reports.financial_reports.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.locadora_rdt_backend.modules.reports.financial_reports.constants.FinancialReportConstants;
import com.locadora_rdt_backend.modules.reports.financial_reports.dto.*;
import com.locadora_rdt_backend.modules.reports.financial_reports.mapper.FinancialReportMapper;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReport;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReportMonth;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReportSummary;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReportTable;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReportType;
import com.locadora_rdt_backend.modules.reports.financial_reports.repository.FinancialReportPayableRepository;
import com.locadora_rdt_backend.modules.reports.financial_reports.repository.FinancialReportReceivableRepository;
import com.locadora_rdt_backend.shared.reports.generator.JasperReportGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FinancialReportServiceImpl implements FinancialReportService {

    private final FinancialReportReceivableRepository receivableRepository;
    private final FinancialReportPayableRepository payableRepository;
    private final FinancialReportMapper mapper;
    private final JasperReportGenerator reportGenerator;

    public FinancialReportServiceImpl(
            FinancialReportReceivableRepository receivableRepository,
            FinancialReportPayableRepository payableRepository,
            FinancialReportMapper mapper,
            JasperReportGenerator reportGenerator
    ) {
        this.receivableRepository = receivableRepository;
        this.payableRepository = payableRepository;
        this.mapper = mapper;
        this.reportGenerator = reportGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialReportFileDTO generate(String reportType, String format, FinancialReportFilterDTO filters) {

        FinancialReportType type = findReportType(reportType);
        String normalizedFormat = normalizeFormat(format);
        FinancialReportFilterDTO normalizedFilters = normalizeFilters(filters);

        FinancialReportTable table = buildReportTable(type, normalizedFilters);
        byte[] data;
        String contentType;

        if (FinancialReportConstants.PDF_FORMAT.equals(normalizedFormat)) {
            data = generatePdf(table);
            contentType = FinancialReportConstants.PDF_CONTENT_TYPE;
        } else {
            data = reportGenerator.generateExcel(table.getTitle(), table.getColumns(), table.getRows());
            contentType = FinancialReportConstants.XLSX_CONTENT_TYPE;
        }

        FinancialReportFileDTO financialReportFileDTO = new FinancialReportFileDTO();
        financialReportFileDTO.setFileName(type.name().toLowerCase()
                + FinancialReportConstants.FILE_EXTENSION_SEPARATOR + normalizedFormat);
        financialReportFileDTO.setContentType(contentType);
        financialReportFileDTO.setData(data);

        return financialReportFileDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialReportDTO comparison(FinancialReportFilterDTO filters) {

        FinancialReportFilterDTO normalizedFilters = normalizeFilters(filters);
        int year = getReportYear(normalizedFilters);

        FinancialReportFilterDTO comparisonFilters = copyFilters(normalizedFilters);
        comparisonFilters.setStartDate(LocalDate.of(year, FinancialReportConstants.FIRST_MONTH,
                FinancialReportConstants.FIRST_DAY_OF_MONTH));
        comparisonFilters.setEndDate(LocalDate.of(year, FinancialReportConstants.LAST_MONTH,
                FinancialReportConstants.LAST_DAY_OF_YEAR));

        List<Receivable> receivables = findReceivables(comparisonFilters);
        List<Payable> payables = findPayables(comparisonFilters);

        BigDecimal receivableTotal = sumReceivables(receivables);
        BigDecimal payableTotal = sumPayables(payables);

        FinancialReport financialReport = new FinancialReport();
        financialReport.setReceivableTotal(receivableTotal);
        financialReport.setPayableTotal(payableTotal);
        financialReport.setBalance(receivableTotal.subtract(payableTotal));
        financialReport.setReceivableCount(receivables.size());
        financialReport.setPayableCount(payables.size());
        financialReport.setYear(year);
        financialReport.setMonths(monthlyComparison(receivables, payables, comparisonFilters.getPeriodType()));

        FinancialReportDTO financialReportDTO = mapper.toDTO(financialReport);

        return financialReportDTO;
    }

    private FinancialReportType findReportType(String reportType) {

        if (reportType == null) {
            throw new IllegalArgumentException(FinancialReportConstants.REPORT_TYPE_REQUIRED);
        }

        String normalized = reportType.trim().replace("-", "_").toUpperCase();

        for (FinancialReportType type : FinancialReportType.values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException(FinancialReportConstants.INVALID_REPORT_TYPE);
    }

    private String normalizeFormat(String format) {

        if (format == null) {
            throw new IllegalArgumentException(FinancialReportConstants.FORMAT_REQUIRED);
        }

        String normalized = format.trim().toLowerCase();

        if (FinancialReportConstants.PDF_FORMAT.equals(normalized)
                || FinancialReportConstants.XLSX_FORMAT.equals(normalized)) {
            return normalized;
        }

        throw new IllegalArgumentException(FinancialReportConstants.INVALID_FORMAT);
    }

    private FinancialReportFilterDTO normalizeFilters(FinancialReportFilterDTO filters) {

        FinancialReportFilterDTO normalized;

        if (filters == null) {
            normalized = new FinancialReportFilterDTO();
        } else {
            normalized = copyFilters(filters);
        }

        String status = normalized.getStatus();
        if (status == null || status.trim().isEmpty()) {
            normalized.setStatus(FinancialReportConstants.STATUS_ALL);
        } else {
            normalized.setStatus(status.trim().replace("-", "_").toUpperCase());
        }

        String periodType = normalized.getPeriodType();
        if (periodType == null || periodType.trim().isEmpty()) {
            normalized.setPeriodType(FinancialReportConstants.PERIOD_DUE_DATE);
        } else {
            normalized.setPeriodType(periodType.trim().replace("-", "_").toUpperCase());
        }

        return normalized;
    }

    private FinancialReportFilterDTO copyFilters(FinancialReportFilterDTO source) {

        FinancialReportFilterDTO copy = new FinancialReportFilterDTO();
        copy.setSearch(source.getSearch());
        copy.setStartDate(source.getStartDate());
        copy.setEndDate(source.getEndDate());
        copy.setStatus(source.getStatus());
        copy.setPeriodType(source.getPeriodType());
        copy.setCustomerId(source.getCustomerId());
        copy.setSupplierId(source.getSupplierId());
        copy.setEmployeeId(source.getEmployeeId());
        copy.setPaymentMethodId(source.getPaymentMethodId());
        copy.setMinimumAmount(source.getMinimumAmount());
        copy.setMaximumAmount(source.getMaximumAmount());
        copy.setYear(source.getYear());

        return copy;
    }

    private int getReportYear(FinancialReportFilterDTO filters) {

        if (filters.getYear() == null) {
            return LocalDate.now().getYear();
        }

        return filters.getYear();
    }

    private List<Receivable> findReceivables(FinancialReportFilterDTO filters) {

        List<Receivable> receivables = receivableRepository.find(
                normalizeSearch(filters.getSearch()),
                dateFilterOrDisabled(filters.getStartDate()),
                dateFilterOrDisabled(filters.getEndDate()),
                filters.getStartDate() != null,
                filters.getEndDate() != null,
                filters.getStatus(),
                filters.getPeriodType(),
                idFilterOrDisabled(filters.getCustomerId()),
                idFilterOrDisabled(filters.getPaymentMethodId()),
                amountFilterOrDisabled(filters.getMinimumAmount()),
                amountFilterOrDisabled(filters.getMaximumAmount())
        );

        return receivables;
    }

    private List<Payable> findPayables(FinancialReportFilterDTO filters) {

        List<Payable> payables = payableRepository.find(
                normalizeSearch(filters.getSearch()),
                dateFilterOrDisabled(filters.getStartDate()),
                dateFilterOrDisabled(filters.getEndDate()),
                filters.getStartDate() != null,
                filters.getEndDate() != null,
                filters.getStatus(),
                filters.getPeriodType(),
                idFilterOrDisabled(filters.getSupplierId()),
                idFilterOrDisabled(filters.getEmployeeId()),
                idFilterOrDisabled(filters.getPaymentMethodId()),
                amountFilterOrDisabled(filters.getMinimumAmount()),
                amountFilterOrDisabled(filters.getMaximumAmount())
        );

        return payables;
    }

    private String normalizeSearch(String search) {

        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        return search.trim();
    }

    private Long idFilterOrDisabled(Long id) {

        if (id == null) {
            return FinancialReportConstants.FILTER_ID_DISABLED;
        }

        return id;
    }

    private BigDecimal amountFilterOrDisabled(BigDecimal amount) {

        if (amount == null) {
            return FinancialReportConstants.FILTER_AMOUNT_DISABLED;
        }

        return amount;
    }

    private LocalDate dateFilterOrDisabled(LocalDate date) {

        if (date == null) {
            return FinancialReportConstants.FILTER_DATE_DISABLED;
        }

        return date;
    }

    private FinancialReportTable buildReportTable(FinancialReportType type, FinancialReportFilterDTO filters) {

        if (type == FinancialReportType.RECEIVABLES) {
            List<Receivable> receivables = findReceivables(filters);
            return receivablesReport(receivables);
        }

        if (type == FinancialReportType.PAYABLES) {
            List<Payable> payables = findPayables(filters);
            return payablesReport(payables);
        }

        if (type == FinancialReportType.FINANCIAL) {
            List<Receivable> receivables = findReceivables(filters);
            List<Payable> payables = findPayables(filters);
            return financialReport(receivables, payables);
        }

        if (type == FinancialReportType.SUMMARY_CUSTOMER) {
            List<Receivable> receivables = findReceivables(filters);
            Map<String, FinancialReportSummary> grouped = groupReceivablesByCustomer(receivables);
            return summaryReport(FinancialReportConstants.CUSTOMER_SUMMARY_REPORT_TITLE,
                    FinancialReportConstants.CUSTOMER_LABEL, grouped);
        }

        if (type == FinancialReportType.SUMMARY_SUPPLIER) {
            List<Payable> payables = findPayables(filters);
            Map<String, FinancialReportSummary> grouped = groupPayablesBySupplier(payables);
            return summaryReport(FinancialReportConstants.SUPPLIER_SUMMARY_REPORT_TITLE,
                    FinancialReportConstants.SUPPLIER_LABEL, grouped);
        }

        if (type == FinancialReportType.SUMMARY_EMPLOYEE) {
            List<Payable> payables = findPayables(filters);
            Map<String, FinancialReportSummary> grouped = groupPayablesByEmployee(payables);
            return summaryReport(FinancialReportConstants.EMPLOYEE_SUMMARY_REPORT_TITLE,
                    FinancialReportConstants.EMPLOYEE_LABEL, grouped);
        }

        return annualBalanceReport(filters);
    }

    private FinancialReportTable receivablesReport(List<Receivable> receivables) {

        List<String> columns = FinancialReportConstants.RECEIVABLE_COLUMNS;
        List<Map<String, ?>> rows = new ArrayList<>();

        for (Receivable receivable : receivables) {
            String customerName = "";
            if (receivable.getCustomer() != null) {
                customerName = text(receivable.getCustomer().getName());
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", String.valueOf(receivable.getId()));
            row.put("column1", text(receivable.getDescription()));
            row.put("column2", customerName);
            row.put("column3", date(receivable.getDueDate()));
            row.put("column4", date(receivable.getPaymentDate()));
            row.put("column5", money(receivable.getAmount()));
            row.put("column6", status(receivable.getPaid(), receivable.getCanceled(), receivable.getDueDate(),
                    receivable.getRemainingBalance(), receivable.getAmount()));
            rows.add(row);
        }

        addTotalRow(rows, sumReceivables(receivables), columns.size());

        FinancialReportTable table = new FinancialReportTable();
        table.setTitle(FinancialReportConstants.RECEIVABLES_REPORT_TITLE);
        table.setColumns(columns);
        table.setRows(rows);

        return table;
    }

    private FinancialReportTable payablesReport(List<Payable> payables) {

        List<String> columns = FinancialReportConstants.PAYABLE_COLUMNS;
        List<Map<String, ?>> rows = new ArrayList<>();

        for (Payable payable : payables) {
            String supplierName = "";
            if (payable.getSupplier() != null) {
                supplierName = text(payable.getSupplier().getName());
            }

            String employeeName = "";
            if (payable.getEmployee() != null) {
                employeeName = text(payable.getEmployee().getName());
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", String.valueOf(payable.getId()));
            row.put("column1", text(payable.getDescription()));
            row.put("column2", supplierName);
            row.put("column3", employeeName);
            row.put("column4", date(payable.getDueDate()));
            row.put("column5", date(payable.getPaymentDate()));
            row.put("column6", money(payable.getAmount()));
            row.put("column7", status(payable.getPaid(), payable.getCanceled(), payable.getDueDate(),
                    payable.getRemainingBalance(), payable.getAmount()));
            rows.add(row);
        }

        addTotalRow(rows, sumPayables(payables), columns.size());

        FinancialReportTable table = new FinancialReportTable();
        table.setTitle(FinancialReportConstants.PAYABLES_REPORT_TITLE);
        table.setColumns(columns);
        table.setRows(rows);

        return table;
    }

    private FinancialReportTable financialReport(List<Receivable> receivables, List<Payable> payables) {

        BigDecimal revenue = sumReceivables(receivables);
        BigDecimal expense = sumPayables(payables);
        BigDecimal received = sumPaidReceivables(receivables);
        BigDecimal paid = sumPaidPayables(payables);
        BigDecimal balance = received.subtract(paid);

        List<String> columns = FinancialReportConstants.FINANCIAL_COLUMNS;
        List<Map<String, ?>> rows = new ArrayList<>();
        String[] labels = {FinancialReportConstants.REVENUE_LABEL, FinancialReportConstants.EXPENSE_LABEL,
                FinancialReportConstants.RECEIVED_TOTAL_LABEL, FinancialReportConstants.PAID_TOTAL_LABEL,
                FinancialReportConstants.BALANCE_LABEL};
        BigDecimal[] values = {revenue, expense, received, paid, balance};

        for (int index = 0; index < labels.length; index++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", labels[index]);
            row.put("column1", money(values[index]));
            rows.add(row);
        }

        FinancialReportTable table = new FinancialReportTable();
        table.setTitle(FinancialReportConstants.FINANCIAL_REPORT_TITLE);
        table.setColumns(columns);
        table.setRows(rows);

        return table;
    }

    private FinancialReportTable summaryReport(String title, String firstColumn,
                                               Map<String, FinancialReportSummary> grouped) {

        List<String> columns = new ArrayList<>();
        columns.add(firstColumn);
        columns.addAll(FinancialReportConstants.SUMMARY_COLUMNS);
        List<Map<String, ?>> rows = new ArrayList<>();

        for (Map.Entry<String, FinancialReportSummary> entry : grouped.entrySet()) {
            FinancialReportSummary values = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", entry.getKey());
            row.put("column1", String.valueOf(values.getQuantity()));
            row.put("column2", money(values.getTotal()));
            row.put("column3", money(values.getPaid()));
            row.put("column4", money(values.getTotal().subtract(values.getPaid())));
            rows.add(row);
        }

        FinancialReportTable table = new FinancialReportTable();
        table.setTitle(title);
        table.setColumns(columns);
        table.setRows(rows);

        return table;
    }

    private FinancialReportTable annualBalanceReport(FinancialReportFilterDTO filters) {

        int year = getReportYear(filters);
        FinancialReportFilterDTO annualFilters = copyFilters(filters);
        annualFilters.setStatus(FinancialReportConstants.STATUS_PAID);
        annualFilters.setPeriodType(FinancialReportConstants.PERIOD_PAYMENT_DATE);
        annualFilters.setStartDate(LocalDate.of(year, FinancialReportConstants.FIRST_MONTH,
                FinancialReportConstants.FIRST_DAY_OF_MONTH));
        annualFilters.setEndDate(LocalDate.of(year, FinancialReportConstants.LAST_MONTH,
                FinancialReportConstants.LAST_DAY_OF_YEAR));

        List<Receivable> receivables = findReceivables(annualFilters);
        List<Payable> payables = findPayables(annualFilters);
        List<String> columns = FinancialReportConstants.ANNUAL_BALANCE_COLUMNS;
        List<Map<String, ?>> rows = new ArrayList<>();
        BigDecimal yearReceived = FinancialReportConstants.ZERO;
        BigDecimal yearPaid = FinancialReportConstants.ZERO;

        for (Month month : Month.values()) {
            BigDecimal received = sumPaidReceivablesByMonth(receivables, month.getValue());
            BigDecimal paid = sumPaidPayablesByMonth(payables, month.getValue());
            yearReceived = yearReceived.add(received);
            yearPaid = yearPaid.add(paid);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", FinancialReportConstants.MONTH_NAMES.get(
                    month.getValue() - FinancialReportConstants.FIRST_MONTH));
            row.put("column1", money(received));
            row.put("column2", money(paid));
            row.put("column3", money(received.subtract(paid)));
            rows.add(row);
        }

        Map<String, Object> totalRow = new LinkedHashMap<>();
        totalRow.put("column0", FinancialReportConstants.YEAR_TOTAL_LABEL);
        totalRow.put("column1", money(yearReceived));
        totalRow.put("column2", money(yearPaid));
        totalRow.put("column3", money(yearReceived.subtract(yearPaid)));
        rows.add(totalRow);

        FinancialReportTable table = new FinancialReportTable();
        table.setTitle(FinancialReportConstants.ANNUAL_BALANCE_REPORT_TITLE + year);
        table.setColumns(columns);
        table.setRows(rows);

        return table;
    }

    private void addTotalRow(List<Map<String, ?>> rows, BigDecimal total, int columns) {

        Map<String, Object> row = new LinkedHashMap<>();

        for (int index = 0; index < columns; index++) {
            row.put("column" + index, "");
        }

        row.put("column0", FinancialReportConstants.TOTAL_LABEL);
        row.put("column" + (columns - 2), money(total));
        rows.add(row);
    }

    private String status(Boolean paid, Boolean canceled, LocalDate dueDate,
                          BigDecimal remainingBalance, BigDecimal amount) {

        if (Boolean.TRUE.equals(canceled)) {
            return FinancialReportConstants.CANCELED_STATUS_LABEL;
        }

        if (Boolean.TRUE.equals(paid)) {
            return FinancialReportConstants.PAID_STATUS_LABEL;
        }

        BigDecimal remaining = valueOrZero(remainingBalance);
        BigDecimal total = valueOrZero(amount);

        if (remaining.compareTo(FinancialReportConstants.ZERO) > 0 && remaining.compareTo(total) < 0) {
            return FinancialReportConstants.PARTIALLY_PAID_STATUS_LABEL;
        }

        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            return FinancialReportConstants.OVERDUE_STATUS_LABEL;
        }

        return FinancialReportConstants.OPEN_STATUS_LABEL;
    }

    private byte[] generatePdf(FinancialReportTable table) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {

            PdfWriter.getInstance(document, output);
            document.open();
            Paragraph title = new Paragraph(table.getTitle(), new Font(Font.HELVETICA, 18, Font.BOLD));
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable pdfTable = new PdfPTable(table.getColumns().size());
            pdfTable.setWidthPercentage(100);
            pdfTable.setHeaderRows(1);

            for (String column : table.getColumns()) {
                PdfPCell cell = new PdfPCell(new Phrase(column, new Font(Font.HELVETICA, 9, Font.BOLD)));
                pdfTable.addCell(cell);
            }

            for (Map<String, ?> row : table.getRows()) {
                for (int index = 0; index < table.getColumns().size(); index++) {
                    Object value = row.get("column" + index);
                    String cellValue = "";
                    if (value != null) {
                        cellValue = value.toString();
                    }
                    pdfTable.addCell(new Phrase(cellValue, new Font(Font.HELVETICA, 9)));
                }
            }

            document.add(pdfTable);
            document.close();

            return output.toByteArray();

        } catch (DocumentException e) {

            throw new FileException(FinancialReportConstants.PDF_GENERATION_ERROR, e);
        }
    }

    private String money(BigDecimal value) {

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valueOrZero(value));
    }

    private String date(LocalDate value) {

        if (value == null) {
            return "";
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern(FinancialReportConstants.DATE_PATTERN);
        return value.format(format);
    }

    private String text(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        if (value == null) {
            return FinancialReportConstants.ZERO;
        }

        return value;
    }

    private BigDecimal sumReceivables(List<Receivable> receivables) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Receivable receivable : receivables) {
            total = total.add(valueOrZero(receivable.getAmount()));
        }

        return total;
    }

    private BigDecimal sumPayables(List<Payable> payables) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Payable payable : payables) {
            total = total.add(valueOrZero(payable.getAmount()));
        }

        return total;
    }

    private BigDecimal sumPaidReceivables(List<Receivable> receivables) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Receivable receivable : receivables) {
            if (Boolean.TRUE.equals(receivable.getPaid())) {
                total = total.add(valueOrZero(receivable.getAmount()));
            }
        }

        return total;
    }

    private BigDecimal sumPaidPayables(List<Payable> payables) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Payable payable : payables) {
            if (Boolean.TRUE.equals(payable.getPaid())) {
                total = total.add(valueOrZero(payable.getAmount()));
            }
        }

        return total;
    }

    private BigDecimal sumPaidReceivablesByMonth(List<Receivable> receivables, int month) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Receivable receivable : receivables) {
            if (Boolean.TRUE.equals(receivable.getPaid()) && receivable.getPaymentDate() != null
                    && receivable.getPaymentDate().getMonthValue() == month) {
                total = total.add(valueOrZero(receivable.getAmount()));
            }
        }

        return total;
    }

    private BigDecimal sumPaidPayablesByMonth(List<Payable> payables, int month) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Payable payable : payables) {
            if (Boolean.TRUE.equals(payable.getPaid()) && payable.getPaymentDate() != null
                    && payable.getPaymentDate().getMonthValue() == month) {
                total = total.add(valueOrZero(payable.getAmount()));
            }
        }

        return total;
    }

    private List<FinancialReportMonth> monthlyComparison(
            List<Receivable> receivables,
            List<Payable> payables,
            String periodType
    ) {
        List<FinancialReportMonth> months = new ArrayList<>();

        for (Month month : Month.values()) {
            FinancialReportMonth reportMonth = new FinancialReportMonth();
            reportMonth.setMonth(month.getValue());
            reportMonth.setLabel(FinancialReportConstants.SHORT_MONTH_NAMES.get(
                    month.getValue() - FinancialReportConstants.FIRST_MONTH));
            reportMonth.setReceivableTotal(sumReceivablesByMonth(receivables, month.getValue(), periodType));
            reportMonth.setPayableTotal(sumPayablesByMonth(payables, month.getValue(), periodType));
            months.add(reportMonth);
        }

        return months;
    }

    private Map<String, FinancialReportSummary> groupReceivablesByCustomer(List<Receivable> receivables) {

        Map<String, FinancialReportSummary> grouped = new LinkedHashMap<>();

        for (Receivable receivable : receivables) {
            String name = FinancialReportConstants.CUSTOMER_NOT_INFORMED;
            if (receivable.getCustomer() != null) {
                name = receivable.getCustomer().getName();
            }
            addSummary(grouped, name, receivable.getAmount(), Boolean.TRUE.equals(receivable.getPaid()));
        }

        return grouped;
    }

    private Map<String, FinancialReportSummary> groupPayablesBySupplier(List<Payable> payables) {

        Map<String, FinancialReportSummary> grouped = new LinkedHashMap<>();

        for (Payable payable : payables) {
            String name = FinancialReportConstants.SUPPLIER_NOT_INFORMED;
            if (payable.getSupplier() != null) {
                name = payable.getSupplier().getName();
            }
            addSummary(grouped, name, payable.getAmount(), Boolean.TRUE.equals(payable.getPaid()));
        }

        return grouped;
    }

    private Map<String, FinancialReportSummary> groupPayablesByEmployee(List<Payable> payables) {

        Map<String, FinancialReportSummary> grouped = new LinkedHashMap<>();

        for (Payable payable : payables) {
            String name = FinancialReportConstants.EMPLOYEE_NOT_INFORMED;
            if (payable.getEmployee() != null) {
                name = payable.getEmployee().getName();
            }
            addSummary(grouped, name, payable.getAmount(), Boolean.TRUE.equals(payable.getPaid()));
        }

        return grouped;
    }

    private void addSummary(Map<String, FinancialReportSummary> grouped, String name, BigDecimal amount, boolean paid) {

        FinancialReportSummary values = grouped.get(name);

        if (values == null) {
            values = new FinancialReportSummary();
            grouped.put(name, values);
        }

        BigDecimal value = valueOrZero(amount);
        values.setQuantity(values.getQuantity() + 1);
        values.setTotal(values.getTotal().add(value));

        if (paid) {
            values.setPaid(values.getPaid().add(value));
        }
    }

    private BigDecimal sumReceivablesByMonth(List<Receivable> receivables, int month, String periodType) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Receivable receivable : receivables) {
            LocalDate date = comparisonDate(receivable, periodType);
            if (date != null && date.getMonthValue() == month) {
                total = total.add(valueOrZero(receivable.getAmount()));
            }
        }

        return total;
    }

    private BigDecimal sumPayablesByMonth(List<Payable> payables, int month, String periodType) {

        BigDecimal total = FinancialReportConstants.ZERO;

        for (Payable payable : payables) {
            LocalDate date = comparisonDate(payable, periodType);
            if (date != null && date.getMonthValue() == month) {
                total = total.add(valueOrZero(payable.getAmount()));
            }
        }

        return total;
    }

    private LocalDate comparisonDate(Receivable receivable, String periodType) {

        if (FinancialReportConstants.PERIOD_PAYMENT_DATE.equals(periodType)) {
            return receivable.getPaymentDate();
        }

        if (FinancialReportConstants.PERIOD_CREATED_DATE.equals(periodType) && receivable.getCreatedAt() != null) {
            return LocalDate.ofInstant(receivable.getCreatedAt(), ZoneOffset.UTC);
        }

        return receivable.getDueDate();
    }

    private LocalDate comparisonDate(Payable payable, String periodType) {

        if (FinancialReportConstants.PERIOD_PAYMENT_DATE.equals(periodType)) {
            return payable.getPaymentDate();
        }

        if (FinancialReportConstants.PERIOD_CREATED_DATE.equals(periodType) && payable.getCreatedAt() != null) {
            return LocalDate.ofInstant(payable.getCreatedAt(), ZoneOffset.UTC);
        }

        return payable.getDueDate();
    }

}
