package com.locadora_rdt_backend.modules.reports.stock_reports.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.reports.stock_reports.constants.StockReportConstants;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportOptionDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportOptionsDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFileDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.mapper.StockReportMapper;
import com.locadora_rdt_backend.modules.reports.stock_reports.model.StockReportSummary;
import com.locadora_rdt_backend.modules.reports.stock_reports.model.StockReportTable;
import com.locadora_rdt_backend.modules.reports.stock_reports.model.StockReportType;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportBalanceRepository;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportBalanceRow;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportMovementRepository;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportUnitRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StockReportServiceImpl implements StockReportService {

    private final StockReportBalanceRepository balanceRepository;
    private final StockReportUnitRepository unitRepository;
    private final StockReportMovementRepository movementRepository;
    private final StockReportMapper mapper;
    private final JasperReportGenerator reportGenerator;

    public StockReportServiceImpl(StockReportBalanceRepository balanceRepository,
                                  StockReportUnitRepository unitRepository,
                                  StockReportMovementRepository movementRepository,
                                  StockReportMapper mapper, JasperReportGenerator reportGenerator) {
        this.balanceRepository = balanceRepository;
        this.unitRepository = unitRepository;
        this.movementRepository = movementRepository;
        this.mapper = mapper;
        this.reportGenerator = reportGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportFileDTO generate(String reportType, String format, StockReportFilterDTO filters) {
        StockReportType type = findReportType(reportType);
        String normalizedFormat = text(format).trim().toLowerCase(Locale.ROOT);
        if (!"pdf".equals(normalizedFormat) && !"xlsx".equals(normalizedFormat)) {
            throw new DatabaseException(StockReportConstants.INVALID_FORMAT);
        }
        StockReportFilterDTO normalizedFilters = normalizeFilters(filters);
        StockReportTable table = buildTable(type, normalizedFilters);
        if (table.getRows().isEmpty()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", "Nenhum registro encontrado.");
            table.getRows().add(row);
        }
        StockReportFileDTO file = new StockReportFileDTO();
        file.setFileName(type.name().toLowerCase(Locale.ROOT) + "." + normalizedFormat);
        if ("pdf".equals(normalizedFormat)) {
            file.setData(generatePdf(table));
            file.setContentType(StockReportConstants.PDF_CONTENT_TYPE);
        } else {
            file.setData(reportGenerator.generateExcel(table.getTitle(), table.getColumns(), table.getRows(), true));
            file.setContentType(StockReportConstants.XLSX_CONTENT_TYPE);
        }
        return file;
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO summary(StockReportFilterDTO filters) {
        List<StockReportBalanceRow> balances = findBalances(normalizeFilters(filters));
        StockReportSummary summary = new StockReportSummary();
        summary.setItemCount(balances.size());
        for (StockReportBalanceRow balance : balances) {
            summary.setTotalQuantity(summary.getTotalQuantity() + balance.getTotalQuantity());
            summary.setAvailableQuantity(summary.getAvailableQuantity() + balance.getAvailableQuantity());
            summary.setUnavailableQuantity(summary.getUnavailableQuantity() + balance.getUnavailableQuantity());
            summary.setMaintenanceQuantity(summary.getMaintenanceQuantity() + balance.getMaintenanceQuantity());
            summary.setDamagedQuantity(summary.getDamagedQuantity() + balance.getDamagedQuantity());
            summary.setLostQuantity(summary.getLostQuantity() + balance.getLostQuantity());
            if (balance.getAvailableQuantity() < balance.getMinimumQuantity()) {
                summary.setLowStockItemCount(summary.getLowStockItemCount() + 1);
            }
        }
        return mapper.toDTO(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportOptionsDTO options() {
        StockReportOptionsDTO options = new StockReportOptionsDTO();
        List<Long> categoryIds = new ArrayList<>();
        for (Item item : balanceRepository.findAll(Sort.by("name"))) {
            Long categoryId = item.getCategory().getId();
            options.getItems().add(new StockReportOptionDTO(item.getId(), item.getName(), categoryId));
            if (!categoryIds.contains(categoryId)) {
                options.getCategories().add(new StockReportOptionDTO(categoryId, item.getCategory().getName(), null));
                categoryIds.add(categoryId);
            }
        }
        return options;
    }

    private StockReportType findReportType(String reportType) {
        String normalized = text(reportType).trim().replace("-", "_").toUpperCase(Locale.ROOT);
        try {
            return StockReportType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new DatabaseException(StockReportConstants.INVALID_REPORT_TYPE);
        }
    }

    private StockReportFilterDTO normalizeFilters(StockReportFilterDTO filters) {
        if (filters == null) {
            filters = new StockReportFilterDTO();
        }
        StockReportFilterDTO normalized = new StockReportFilterDTO();
        normalized.setSearch(text(filters.getSearch()).trim());
        normalized.setCategoryId(idFilter(filters.getCategoryId()));
        normalized.setItemId(idFilter(filters.getItemId()));
        normalized.setActive(filters.getActive());
        normalized.setStatus(normalizeOption(filters.getStatus()));
        normalized.setConditionStatus(normalizeOption(filters.getConditionStatus()));
        normalized.setMovementType(normalizeOption(filters.getMovementType()));
        normalized.setStartDate(filters.getStartDate());
        normalized.setEndDate(filters.getEndDate());
        if (normalized.getStartDate() != null && normalized.getEndDate() != null
                && normalized.getStartDate().isAfter(normalized.getEndDate())) {
            throw new DatabaseException(StockReportConstants.INVALID_PERIOD);
        }
        try {
            if (!StockReportConstants.ALL.equals(normalized.getStatus())) {
                ItemUnitStatus.valueOf(normalized.getStatus());
            }
            if (!StockReportConstants.ALL.equals(normalized.getConditionStatus())) {
                ItemUnitCondition.valueOf(normalized.getConditionStatus());
            }
            if (!StockReportConstants.ALL.equals(normalized.getMovementType())) {
                StockMovementType.valueOf(normalized.getMovementType());
            }
        } catch (IllegalArgumentException e) {
            throw new DatabaseException(StockReportConstants.INVALID_FILTER);
        }
        return normalized;
    }

    private Long idFilter(Long id) {
        if (id == null) {
            return -1L;
        }
        if (id <= 0) {
            throw new DatabaseException(StockReportConstants.INVALID_FILTER);
        }
        return id;
    }

    private String normalizeOption(String option) {
        if (option == null || option.trim().isEmpty()) {
            return StockReportConstants.ALL;
        }
        return option.trim().replace("-", "_").toUpperCase(Locale.ROOT);
    }

    private int activeFilter(Boolean active) {
        if (active == null) {
            return -1;
        }
        return active ? 1 : 0;
    }

    private List<StockReportBalanceRow> findBalances(StockReportFilterDTO filters) {
        return balanceRepository.find(filters.getSearch(), filters.getCategoryId(), filters.getItemId(),
                activeFilter(filters.getActive()));
    }

    private StockReportTable buildTable(StockReportType type, StockReportFilterDTO filters) {
        if (type == StockReportType.BALANCES || type == StockReportType.LOW_STOCK) {
            return balancesTable(type, filters);
        }
        if (type == StockReportType.ITEM_UNITS) {
            return unitsTable(filters);
        }
        return movementsTable(filters);
    }

    private StockReportTable balancesTable(StockReportType type, StockReportFilterDTO filters) {
        List<Map<String, ?>> rows = new ArrayList<>();
        for (StockReportBalanceRow balance : findBalances(filters)) {
            boolean lowStock = balance.getAvailableQuantity() < balance.getMinimumQuantity();
            if (type == StockReportType.LOW_STOCK && !lowStock) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", text(balance.getItemName()));
            row.put("column1", text(balance.getCategoryName()));
            row.put("column2", String.valueOf(balance.getTotalQuantity()));
            row.put("column3", String.valueOf(balance.getAvailableQuantity()));
            row.put("column4", String.valueOf(balance.getUnavailableQuantity()));
            row.put("column5", String.valueOf(balance.getMaintenanceQuantity()));
            row.put("column6", String.valueOf(balance.getDamagedQuantity()));
            row.put("column7", String.valueOf(balance.getLostQuantity()));
            row.put("column8", String.valueOf(balance.getMinimumQuantity()));
            row.put("column9", lowStock ? "Sim" : "Não");
            rows.add(row);
        }
        String title = "Saldos atuais de estoque";
        if (type == StockReportType.LOW_STOCK) {
            title = "Estoque abaixo do mínimo";
        }
        return table(title, StockReportConstants.BALANCE_COLUMNS, rows);
    }

    private StockReportTable unitsTable(StockReportFilterDTO filters) {
        List<ItemUnit> units = unitRepository.find(filters.getSearch(), filters.getCategoryId(), filters.getItemId(),
                activeFilter(filters.getActive()), filters.getStatus(), filters.getConditionStatus());
        List<Map<String, ?>> rows = new ArrayList<>();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (ItemUnit unit : units) {
            String availability = statusLabel(unit.getStatus());
            if (!Boolean.TRUE.equals(unit.getActive())) {
                availability = "Com baixa";
            } else if (unit.getStatus() == ItemUnitStatus.AVAILABLE
                    && (!Boolean.TRUE.equals(unit.getItem().getActive())
                    || !Boolean.TRUE.equals(unit.getItem().getCategory().getActive()))) {
                availability = "Indisponível";
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", text(unit.getItem().getName()));
            row.put("column1", text(unit.getItem().getCategory().getName()));
            row.put("column2", text(unit.getAssetCode()));
            row.put("column3", availability);
            row.put("column4", conditionLabel(unit.getConditionStatus()));
            row.put("column5", unit.getPurchaseDate() == null ? "" : dateFormat.format(unit.getPurchaseDate()));
            row.put("column6", Boolean.TRUE.equals(unit.getActive()) ? "Sim" : "Não");
            rows.add(row);
        }
        return table("Unidades físicas de estoque", StockReportConstants.UNIT_COLUMNS, rows);
    }

    private StockReportTable movementsTable(StockReportFilterDTO filters) {
        LocalDate startDate = filters.getStartDate();
        if (startDate == null) {
            startDate = StockReportConstants.DISABLED_DATE;
        }
        LocalDate endDate = filters.getEndDate();
        if (endDate == null) {
            endDate = StockReportConstants.DISABLED_DATE;
        }
        Instant startAt = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endAt = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<StockMovement> movements = movementRepository.find(filters.getSearch(), filters.getCategoryId(),
                filters.getItemId(), filters.getMovementType(), startAt, endAt,
                filters.getStartDate() != null, filters.getEndDate() != null);
        List<Map<String, ?>> rows = new ArrayList<>();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.UTC);
        for (StockMovement movement : movements) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("column0", movement.getCreatedAt() == null ? "" : dateFormat.format(movement.getCreatedAt()));
            row.put("column1", text(movement.getItem().getName()));
            row.put("column2", movement.getItemUnit() == null ? "" : text(movement.getItemUnit().getAssetCode()));
            row.put("column3", movementLabel(movement.getType()));
            row.put("column4", String.valueOf(movement.getQuantity()));
            row.put("column5", text(movement.getReason()));
            row.put("column6", text(movement.getCreatedBy()));
            row.put("column7", statusLabel(movement.getPreviousStatus()));
            row.put("column8", statusLabel(movement.getNewStatus()));
            rows.add(row);
        }
        return table("Movimentações de estoque", StockReportConstants.MOVEMENT_COLUMNS, rows);
    }

    private StockReportTable table(String title, List<String> columns, List<Map<String, ?>> rows) {
        StockReportTable table = new StockReportTable();
        table.setTitle(title);
        table.setColumns(columns);
        table.setRows(rows);
        return table;
    }

    private String statusLabel(ItemUnitStatus status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case AVAILABLE:
                return "Disponível";
            case UNAVAILABLE:
                return "Indisponível";
            case MAINTENANCE:
                return "Em manutenção";
            case DAMAGED:
                return "Danificada";
            case LOST:
                return "Não localizada";
            default:
                return status.name();
        }
    }

    private String conditionLabel(ItemUnitCondition condition) {
        if (condition == null) {
            return "";
        }
        switch (condition) {
            case NEW:
                return "Nova";
            case GOOD:
                return "Boa";
            case FAIR:
                return "Regular";
            case DAMAGED:
                return "Danificada";
            default:
                return condition.name();
        }
    }

    private String movementLabel(StockMovementType type) {
        switch (type) {
            case ENTRY:
                return "Entrada";
            case EXIT:
                return "Saída";
            case ADJUSTMENT:
                return "Ajuste (total final)";
            case STATUS_CHANGE:
                return "Alteração de situação";
            default:
                return type.name();
        }
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private byte[] generatePdf(StockReportTable table) {
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
                pdfTable.addCell(new PdfPCell(new Phrase(column, new Font(Font.HELVETICA, 8, Font.BOLD))));
            }
            for (Map<String, ?> row : table.getRows()) {
                for (int index = 0; index < table.getColumns().size(); index++) {
                    Object value = row.get("column" + index);
                    pdfTable.addCell(new Phrase(value == null ? "" : value.toString(), new Font(Font.HELVETICA, 8)));
                }
            }
            document.add(pdfTable);
            document.close();
            return output.toByteArray();
        } catch (DocumentException e) {
            throw new FileException(StockReportConstants.PDF_GENERATION_ERROR, e);
        } finally {
            document.close();
        }
    }
}
