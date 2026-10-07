package com.locadora_rdt_backend.modules.reports.stock_reports.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.modules.reports.stock_reports.constants.StockReportConstants;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFileDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportOptionsDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.mapper.StockReportMapper;
import com.locadora_rdt_backend.modules.reports.stock_reports.model.StockReportSummary;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportBalanceRepository;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportBalanceRow;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportMovementRepository;
import com.locadora_rdt_backend.modules.reports.stock_reports.repository.StockReportUnitRepository;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.shared.reports.generator.JasperReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockReportServiceTests {
    @Mock private StockReportBalanceRepository balanceRepository;
    @Mock private StockReportUnitRepository unitRepository;
    @Mock private StockReportMovementRepository movementRepository;
    @Mock private StockReportMapper mapper;
    @Mock private JasperReportGenerator reportGenerator;
    @Mock private StockReportBalanceRow balance;
    @InjectMocks private StockReportServiceImpl service;
    private StockReportFilterDTO filters;

    @BeforeEach
    void setUp() {
        filters = new StockReportFilterDTO();
        filters.setSearch(" Furadeira ");
        filters.setCategoryId(1L);
        filters.setItemId(2L);
        filters.setActive(false);
    }

    @Test
    void generateShouldReturnExcelAndPreserveFilters() {
        when(balanceRepository.find("Furadeira", 1L, 2L, 0)).thenReturn(Collections.singletonList(balance));
        when(balance.getItemName()).thenReturn("Furadeira");
        when(balance.getCategoryName()).thenReturn("Ferramentas");
        when(balance.getTotalQuantity()).thenReturn(5L);
        when(balance.getAvailableQuantity()).thenReturn(2L);
        when(balance.getUnavailableQuantity()).thenReturn(1L);
        when(balance.getMaintenanceQuantity()).thenReturn(1L);
        when(balance.getDamagedQuantity()).thenReturn(1L);
        when(balance.getLostQuantity()).thenReturn(0L);
        when(balance.getMinimumQuantity()).thenReturn(3);
        when(reportGenerator.generateExcel(anyString(), anyList(), anyList(), eq(true))).thenCallRealMethod();

        StockReportFileDTO result = service.generate(" BALANCES ", "XLSX", filters);

        assertEquals("balances.xlsx", result.getFileName());
        assertEquals(StockReportConstants.XLSX_CONTENT_TYPE, result.getContentType());
        assertEquals('P', result.getData()[0]);
        assertEquals('K', result.getData()[1]);
        assertEquals(" Furadeira ", filters.getSearch());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, ?>>> captor = ArgumentCaptor.forClass(List.class);
        verify(reportGenerator).generateExcel(eq("Saldos atuais de estoque"),
                eq(StockReportConstants.BALANCE_COLUMNS), captor.capture(), eq(true));
        Map<String, ?> row = captor.getValue().get(0);
        assertEquals("5", row.get("column2"));
        assertEquals("2", row.get("column3"));
        assertEquals("Sim", row.get("column9"));
    }

    @Test
    void generateShouldThrowExceptionWhenReportTypeIsInvalid() {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.generate("invalid", "pdf", filters));
        assertEquals(StockReportConstants.INVALID_REPORT_TYPE, exception.getMessage());
        verifyNoInteractions(balanceRepository, unitRepository, movementRepository, reportGenerator);
    }

    @Test
    void generateShouldThrowExceptionWhenFormatIsInvalid() {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.generate("balances", "csv", filters));
        assertEquals(StockReportConstants.INVALID_FORMAT, exception.getMessage());
        verifyNoInteractions(balanceRepository, unitRepository, movementRepository, reportGenerator);
    }

    @Test
    void generateShouldThrowExceptionWhenPeriodIsInvalid() {
        filters.setStartDate(LocalDate.of(2026, 2, 1));
        filters.setEndDate(LocalDate.of(2026, 1, 1));
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.generate("movements", "pdf", filters));
        assertEquals(StockReportConstants.INVALID_PERIOD, exception.getMessage());
        verifyNoInteractions(movementRepository);
    }

    @Test
    void generateShouldThrowExceptionWhenStatusIsInvalid() {
        filters.setStatus("RENTED");
        assertThrows(DatabaseException.class, () -> service.generate("item-units", "xlsx", filters));
        verifyNoInteractions(unitRepository);
    }

    @Test
    void generateShouldThrowExceptionWhenConditionIsInvalid() {
        filters.setConditionStatus("INVALID");
        assertThrows(DatabaseException.class, () -> service.generate("item-units", "pdf", filters));
        verifyNoInteractions(unitRepository);
    }

    @Test
    void generateShouldThrowExceptionWhenMovementTypeIsInvalid() {
        filters.setMovementType("SALE");
        assertThrows(DatabaseException.class, () -> service.generate("movements", "pdf", filters));
        verifyNoInteractions(movementRepository);
    }

    @Test
    void summaryShouldReturnQuantitiesAndLowStockCount() {
        when(balanceRepository.find("Furadeira", 1L, 2L, 0)).thenReturn(Collections.singletonList(balance));
        when(balance.getTotalQuantity()).thenReturn(5L);
        when(balance.getAvailableQuantity()).thenReturn(2L);
        when(balance.getUnavailableQuantity()).thenReturn(1L);
        when(balance.getMaintenanceQuantity()).thenReturn(1L);
        when(balance.getDamagedQuantity()).thenReturn(1L);
        when(balance.getLostQuantity()).thenReturn(0L);
        when(balance.getMinimumQuantity()).thenReturn(3);
        when(mapper.toDTO(any(StockReportSummary.class))).thenCallRealMethod();

        StockReportDTO result = service.summary(filters);

        assertEquals(1, result.getItemCount());
        assertEquals(5, result.getTotalQuantity());
        assertEquals(2, result.getAvailableQuantity());
        assertEquals(1, result.getUnavailableQuantity());
        assertEquals(1, result.getMaintenanceQuantity());
        assertEquals(1, result.getDamagedQuantity());
        assertEquals(0, result.getLostQuantity());
        assertEquals(1, result.getLowStockItemCount());
    }

    @Test
    void summaryShouldThrowExceptionWhenRepositoryFails() {
        when(balanceRepository.find("Furadeira", 1L, 2L, 0))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));
        assertThrows(DataAccessResourceFailureException.class, () -> service.summary(filters));
        verifyNoInteractions(mapper);
    }

    @Test
    void summaryShouldReturnZeroWithoutFiltersAndWithoutItems() {
        when(balanceRepository.find("", -1L, -1L, -1)).thenReturn(Collections.emptyList());
        when(mapper.toDTO(any(StockReportSummary.class))).thenCallRealMethod();
        StockReportDTO result = service.summary(null);
        assertEquals(0, result.getItemCount());
        assertEquals(0, result.getTotalQuantity());
        assertEquals(0, result.getLowStockItemCount());
    }

    @Test
    void optionsShouldReturnItemsAndCategoriesWithoutDuplicates() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Ferramentas");
        Item item = new Item();
        item.setId(2L);
        item.setName("Furadeira");
        item.setCategory(category);
        Item secondItem = new Item();
        secondItem.setId(3L);
        secondItem.setName("Serra");
        secondItem.setCategory(category);
        when(balanceRepository.findAll(Sort.by("name"))).thenReturn(Arrays.asList(item, secondItem));

        StockReportOptionsDTO result = service.options();

        assertEquals(1, result.getCategories().size());
        assertEquals(2, result.getItems().size());
        assertEquals("Ferramentas", result.getCategories().get(0).getName());
        assertEquals(1L, result.getItems().get(0).getCategoryId());
    }

    @Test
    void optionsShouldThrowExceptionWhenRepositoryFails() {
        when(balanceRepository.findAll(Sort.by("name")))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));
        assertThrows(DataAccessResourceFailureException.class, () -> service.options());
    }
}
