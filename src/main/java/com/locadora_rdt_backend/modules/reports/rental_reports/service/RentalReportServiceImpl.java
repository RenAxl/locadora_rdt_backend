package com.locadora_rdt_backend.modules.reports.rental_reports.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.reports.rental_reports.constants.RentalReportConstants;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFileDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.mapper.RentalReportMapper;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReport;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReportMonth;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReportSummary;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReportTable;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReportType;
import com.locadora_rdt_backend.modules.reports.rental_reports.repository.RentalReportRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RentalReportServiceImpl implements RentalReportService {

    private final RentalReportRepository repository;
    private final RentalReportMapper mapper;
    private final JasperReportGenerator reportGenerator;

    public RentalReportServiceImpl(
            RentalReportRepository repository,
            RentalReportMapper mapper,
            JasperReportGenerator reportGenerator
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.reportGenerator = reportGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public RentalReportFileDTO generate(String reportType, String format, RentalReportFilterDTO filters) {

        RentalReportType type = findReportType(reportType);
        String normalizedFormat = normalizeFormat(format);
        RentalReportFilterDTO normalizedFilters = normalizeFilters(filters);
        RentalReportTable table = buildReportTable(type, normalizedFilters);
        byte[] data;
        String contentType;

        if (RentalReportConstants.PDF_FORMAT.equals(normalizedFormat)) {
            data = generatePdf(table);
            contentType = RentalReportConstants.PDF_CONTENT_TYPE;
        } else {
            data = reportGenerator.generateExcel(table.getTitle(), table.getColumns(), table.getRows(), true);
            contentType = RentalReportConstants.XLSX_CONTENT_TYPE;
        }

        RentalReportFileDTO dto = new RentalReportFileDTO();
        dto.setFileName(type.name().toLowerCase(Locale.ROOT)
                + RentalReportConstants.FILE_EXTENSION_SEPARATOR + normalizedFormat);
        dto.setContentType(contentType);
        dto.setData(data);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public RentalReportDTO comparison(RentalReportFilterDTO filters) {

        RentalReportFilterDTO normalizedFilters = normalizeFilters(filters);
        RentalReport report = annualReport(normalizedFilters);
        RentalReportDTO dto = mapper.toDTO(report);

        return dto;
    }

    private RentalReportType findReportType(String reportType) {

        if (reportType == null) {
            throw new DatabaseException(RentalReportConstants.REPORT_TYPE_REQUIRED);
        }

        String normalized = reportType.trim().replace("-", "_").toUpperCase(Locale.ROOT);

        for (RentalReportType type : RentalReportType.values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        throw new DatabaseException(RentalReportConstants.INVALID_REPORT_TYPE);
    }

    private String normalizeFormat(String format) {

        if (format == null) {
            throw new DatabaseException(RentalReportConstants.FORMAT_REQUIRED);
        }

        String normalized = format.trim().toLowerCase(Locale.ROOT);

        if (RentalReportConstants.PDF_FORMAT.equals(normalized)
                || RentalReportConstants.XLSX_FORMAT.equals(normalized)) {
            return normalized;
        }

        throw new DatabaseException(RentalReportConstants.INVALID_FORMAT);
    }

    private RentalReportFilterDTO normalizeFilters(RentalReportFilterDTO filters) {

        RentalReportFilterDTO normalized = new RentalReportFilterDTO();
        if (filters != null) {
            normalized = copyFilters(filters);
        }

        if (normalized.getSearch() != null) {
            normalized.setSearch(normalized.getSearch().trim());
            if (normalized.getSearch().isEmpty()) {
                normalized.setSearch(null);
            }
        }

        String status = normalized.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = RentalReportConstants.STATUS_ALL;
        } else {
            status = status.trim().toUpperCase(Locale.ROOT);
        }
        if (!RentalReportConstants.STATUS_ALL.equals(status)
                && !RentalConstants.STATUS_RENTED.equals(status)
                && !RentalConstants.STATUS_DELIVERED.equals(status)
                && !RentalConstants.STATUS_RESERVED.equals(status)) {
            throw new DatabaseException("Situação da locação inválida.");
        }
        normalized.setStatus(status);

        String periodType = normalized.getPeriodType();
        if (periodType == null || periodType.trim().isEmpty()) {
            periodType = RentalReportConstants.PERIOD_REGISTRATION_DATE;
        } else {
            periodType = periodType.trim().replace("-", "_").toUpperCase(Locale.ROOT);
        }
        if (!RentalReportConstants.PERIOD_REGISTRATION_DATE.equals(periodType)
                && !RentalReportConstants.PERIOD_RENTAL_START_DATE.equals(periodType)
                && !RentalReportConstants.PERIOD_RETURN_FORECAST_DATE.equals(periodType)
                && !RentalReportConstants.PERIOD_EFFECTIVE_RETURN_DATE.equals(periodType)) {
            throw new DatabaseException("Tipo de período inválido.");
        }
        normalized.setPeriodType(periodType);

        if (normalized.getStartDate() != null && normalized.getEndDate() != null
                && normalized.getStartDate().isAfter(normalized.getEndDate())) {
            throw new DatabaseException("Data inicial não pode ser maior que a data final.");
        }
        if (normalized.getMinimumAmount() != null && normalized.getMinimumAmount().signum() < 0
                || normalized.getMaximumAmount() != null && normalized.getMaximumAmount().signum() < 0) {
            throw new DatabaseException("Os valores não podem ser negativos.");
        }
        if (normalized.getMinimumAmount() != null && normalized.getMaximumAmount() != null
                && normalized.getMinimumAmount().compareTo(normalized.getMaximumAmount()) > 0) {
            throw new DatabaseException("Valor inicial não pode ser maior que o valor final.");
        }
        if (normalized.getYear() != null && (normalized.getYear() < 1900 || normalized.getYear() > 9999)) {
            throw new DatabaseException("Informe um ano entre 1900 e 9999.");
        }

        return normalized;
    }

    private RentalReportFilterDTO copyFilters(RentalReportFilterDTO source) {

        RentalReportFilterDTO copy = new RentalReportFilterDTO();
        copy.setSearch(source.getSearch());
        copy.setStartDate(source.getStartDate());
        copy.setEndDate(source.getEndDate());
        copy.setStatus(source.getStatus());
        copy.setPeriodType(source.getPeriodType());
        copy.setCustomerId(source.getCustomerId());
        copy.setRentalTypeId(source.getRentalTypeId());
        copy.setPaymentMethodId(source.getPaymentMethodId());
        copy.setMinimumAmount(source.getMinimumAmount());
        copy.setMaximumAmount(source.getMaximumAmount());
        copy.setYear(source.getYear());

        return copy;
    }

    private List<Rental> findRentals(RentalReportFilterDTO filters) {

        LocalDate startDate = filters.getStartDate();
        if (startDate == null) {
            startDate = RentalReportConstants.FILTER_DATE_DISABLED;
        }
        LocalDate endDate = filters.getEndDate();
        if (endDate == null) {
            endDate = RentalReportConstants.FILTER_DATE_DISABLED;
        }
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Rental> rentals = repository.find(
                filters.getSearch(), start, end,
                filters.getStartDate() != null, filters.getEndDate() != null,
                filters.getStatus(), filters.getPeriodType(),
                idFilterOrDisabled(filters.getCustomerId()),
                idFilterOrDisabled(filters.getRentalTypeId()),
                idFilterOrDisabled(filters.getPaymentMethodId()),
                amountFilterOrDisabled(filters.getMinimumAmount()),
                amountFilterOrDisabled(filters.getMaximumAmount())
        );

        return rentals;
    }

    private Long idFilterOrDisabled(Long id) {

        if (id == null) {
            return RentalReportConstants.FILTER_ID_DISABLED;
        }

        return id;
    }

    private BigDecimal amountFilterOrDisabled(BigDecimal amount) {

        if (amount == null) {
            return RentalReportConstants.FILTER_AMOUNT_DISABLED;
        }

        return amount;
    }

    private RentalReportTable buildReportTable(RentalReportType type, RentalReportFilterDTO filters) {

        if (type == RentalReportType.ANNUAL_SUMMARY) {
            return annualSummaryReport(filters);
        }

        List<Rental> rentals = findRentals(filters);
        if (type == RentalReportType.SUMMARY_CUSTOMER) {
            return customerSummaryReport(rentals);
        }

        return rentalsReport(rentals);
    }

    private RentalReportTable rentalsReport(List<Rental> rentals) {

        List<Map<String, ?>> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Rental rental : rentals) {
            String customerName = "";
            if (rental.getCustomer() != null) {
                customerName = text(rental.getCustomer().getName());
            }
            String rentalTypeName = "";
            if (rental.getRentalType() != null) {
                rentalTypeName = text(rental.getRentalType().getName());
            }
            String paymentMethodName = "";
            if (rental.getPaymentMethod() != null) {
                paymentMethodName = text(rental.getPaymentMethod().getName());
            }
            String paid = "Não";
            if (Boolean.TRUE.equals(rental.getPaid())) {
                paid = "Sim";
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", text(rental.getRentalNumber()));
            row.put("column1", customerName);
            row.put("column2", rentalTypeName);
            row.put("column3", status(rental.getStatus()));
            row.put("column4", date(rental.getRegistrationDate()));
            row.put("column5", date(rental.getRentalStartDate()));
            row.put("column6", date(rental.getReturnForecastDate()));
            row.put("column7", date(rental.getEffectiveReturnDate()));
            row.put("column8", paymentMethodName);
            row.put("column9", paid);
            row.put("column10", money(recordedAmount(rental)));
            rows.add(row);
            total = total.add(recordedAmount(rental));
        }

        Map<String, Object> totalRow = new LinkedHashMap<>();
        totalRow.put("column0", "Total");
        totalRow.put("column10", money(total));
        rows.add(totalRow);

        RentalReportTable table = new RentalReportTable();
        table.setTitle(RentalReportConstants.RENTALS_REPORT_TITLE);
        table.setColumns(RentalReportConstants.RENTAL_COLUMNS);
        table.setRows(rows);

        return table;
    }

    private RentalReportTable customerSummaryReport(List<Rental> rentals) {

        Map<Long, RentalReportSummary> grouped = new LinkedHashMap<>();
        for (Rental rental : rentals) {
            Long customerId = null;
            String name = "Sem cliente";
            if (rental.getCustomer() != null) {
                customerId = rental.getCustomer().getId();
                name = text(rental.getCustomer().getName());
            }
            RentalReportSummary summary = grouped.get(customerId);
            if (summary == null) {
                summary = new RentalReportSummary();
                summary.setName(name);
                grouped.put(customerId, summary);
            }
            summary.setQuantity(summary.getQuantity() + 1);
            summary.setTotal(summary.getTotal().add(recordedAmount(rental)));
            if (Boolean.TRUE.equals(rental.getPaid())) {
                summary.setPaid(summary.getPaid().add(recordedAmount(rental)));
            }
        }

        List<Map<String, ?>> rows = new ArrayList<>();
        for (RentalReportSummary summary : grouped.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", summary.getName());
            row.put("column1", String.valueOf(summary.getQuantity()));
            row.put("column2", money(summary.getTotal()));
            row.put("column3", money(summary.getPaid()));
            rows.add(row);
        }

        RentalReportTable table = new RentalReportTable();
        table.setTitle(RentalReportConstants.CUSTOMER_SUMMARY_REPORT_TITLE);
        table.setColumns(RentalReportConstants.SUMMARY_COLUMNS);
        table.setRows(rows);

        return table;
    }

    private RentalReport annualReport(RentalReportFilterDTO filters) {

        int year = LocalDate.now().getYear();
        if (filters.getYear() != null) {
            year = filters.getYear();
        }
        RentalReportFilterDTO annualFilters = copyFilters(filters);
        annualFilters.setStartDate(LocalDate.of(year, 1, 1));
        annualFilters.setEndDate(LocalDate.of(year, 12, 31));
        List<Rental> rentals = findRentals(annualFilters);

        RentalReport report = new RentalReport();
        report.setYear(year);
        report.setRentalCount(rentals.size());
        report.setPaidCount(0);
        report.setRentalTotal(BigDecimal.ZERO);
        report.setPaidTotal(BigDecimal.ZERO);

        for (Rental rental : rentals) {
            BigDecimal amount = recordedAmount(rental);
            report.setRentalTotal(report.getRentalTotal().add(amount));
            if (Boolean.TRUE.equals(rental.getPaid())) {
                report.setPaidCount(report.getPaidCount() + 1);
                report.setPaidTotal(report.getPaidTotal().add(amount));
            }
        }

        List<RentalReportMonth> months = new ArrayList<>();
        for (Month month : Month.values()) {
            RentalReportMonth reportMonth = new RentalReportMonth();
            reportMonth.setMonth(month.getValue());
            reportMonth.setLabel(RentalReportConstants.SHORT_MONTH_NAMES.get(month.getValue() - 1));
            reportMonth.setRentalTotal(BigDecimal.ZERO);
            reportMonth.setPaidTotal(BigDecimal.ZERO);

            for (Rental rental : rentals) {
                Instant date = comparisonDate(rental, filters.getPeriodType());
                if (date != null && LocalDate.ofInstant(date, ZoneId.systemDefault()).getMonthValue() == month.getValue()) {
                    BigDecimal amount = recordedAmount(rental);
                    reportMonth.setRentalTotal(reportMonth.getRentalTotal().add(amount));
                    if (Boolean.TRUE.equals(rental.getPaid())) {
                        reportMonth.setPaidTotal(reportMonth.getPaidTotal().add(amount));
                    }
                }
            }
            months.add(reportMonth);
        }
        report.setMonths(months);

        return report;
    }

    private RentalReportTable annualSummaryReport(RentalReportFilterDTO filters) {

        RentalReport report = annualReport(filters);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (RentalReportMonth month : report.getMonths()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", RentalReportConstants.MONTH_NAMES.get(month.getMonth() - 1));
            row.put("column1", money(month.getRentalTotal()));
            row.put("column2", money(month.getPaidTotal()));
            rows.add(row);
        }
        Map<String, Object> totalRow = new LinkedHashMap<>();
        totalRow.put("column0", "Total do ano");
        totalRow.put("column1", money(report.getRentalTotal()));
        totalRow.put("column2", money(report.getPaidTotal()));
        rows.add(totalRow);

        RentalReportTable table = new RentalReportTable();
        table.setTitle(RentalReportConstants.ANNUAL_SUMMARY_REPORT_TITLE + report.getYear());
        table.setColumns(RentalReportConstants.ANNUAL_COLUMNS);
        table.setRows(rows);

        return table;
    }

    private Instant comparisonDate(Rental rental, String periodType) {

        if (RentalReportConstants.PERIOD_RENTAL_START_DATE.equals(periodType)) {
            return rental.getRentalStartDate();
        }
        if (RentalReportConstants.PERIOD_RETURN_FORECAST_DATE.equals(periodType)) {
            return rental.getReturnForecastDate();
        }
        if (RentalReportConstants.PERIOD_EFFECTIVE_RETURN_DATE.equals(periodType)) {
            return rental.getEffectiveReturnDate();
        }

        return rental.getRegistrationDate();
    }

    private String status(String value) {

        if (RentalConstants.STATUS_RENTED.equals(value)) {
            return "Alugada";
        }
        if (RentalConstants.STATUS_DELIVERED.equals(value)) {
            return "Entregue";
        }
        if (RentalConstants.STATUS_RESERVED.equals(value)) {
            return "Reservada";
        }

        return text(value);
    }

    private BigDecimal recordedAmount(Rental rental) {

        return valueOrZero(rental.getTotalAmount())
                .add(valueOrZero(rental.getLateFee()))
                .subtract(valueOrZero(rental.getDiscount()));
    }

    private byte[] generatePdf(RentalReportTable table) {

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

            throw new FileException(RentalReportConstants.PDF_GENERATION_ERROR, e);
        }
    }

    private String money(BigDecimal value) {

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valueOrZero(value));
    }

    private String date(Instant value) {

        if (value == null) {
            return "";
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern(RentalReportConstants.DATE_PATTERN)
                .withZone(ZoneId.systemDefault());
        return format.format(value);
    }

    private String text(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value;
    }

}
