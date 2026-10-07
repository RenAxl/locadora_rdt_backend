package com.locadora_rdt_backend.modules.reports.stock_reports.repository;

import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFileDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.mapper.StockReportMapper;
import com.locadora_rdt_backend.modules.reports.stock_reports.service.StockReportServiceImpl;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.categories.repository.CategoryRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.StockQuantitySummary;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import com.locadora_rdt_backend.modules.stocks.stock_movements.repository.StockMovementRepository;
import com.locadora_rdt_backend.shared.reports.generator.JasperReportGenerator;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({StockReportServiceImpl.class, StockReportMapper.class, JasperReportGenerator.class})
class StockReportRepositoryTests {
    @Autowired private StockReportBalanceRepository balances;
    @Autowired private StockReportUnitRepository units;
    @Autowired private StockReportMovementRepository movements;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemUnitRepository unitRepository;
    @Autowired private StockBalanceRepository balanceRepository;
    @Autowired private StockMovementRepository movementRepository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private StockReportServiceImpl service;
    private Category category;
    private Item item;
    private ItemUnit retiredUnit;
    private int code;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Ferramentas de relatório");
        category.setCreatedBy("tester");
        categoryRepository.saveAndFlush(category);
        item = new Item();
        item.setName("Furadeira de relatório");
        item.setDescription("Equipamento de teste");
        item.setCategory(category);
        item.setActive(true);
        item.setCreatedBy("tester");
        itemRepository.saveAndFlush(item);
        StockBalance balance = new StockBalance();
        balance.setItem(item);
        balance.setMinimumQuantity(2);
        balance.setCreatedBy("tester");
        balanceRepository.saveAndFlush(balance);
        createUnit(ItemUnitStatus.AVAILABLE, true);
        createUnit(ItemUnitStatus.UNAVAILABLE, true);
        createUnit(ItemUnitStatus.MAINTENANCE, true);
        createUnit(ItemUnitStatus.DAMAGED, true);
        createUnit(ItemUnitStatus.LOST, true);
        retiredUnit = createUnit(ItemUnitStatus.AVAILABLE, false);
        createMovement(StockMovementType.ENTRY, null, "2026-01-01T00:00:00Z");
        createMovement(StockMovementType.EXIT, retiredUnit, "2026-01-31T23:59:59Z");
        createMovement(StockMovementType.ADJUSTMENT, null, "2026-02-01T00:00:00Z");
    }

    @Test
    void balancesShouldMatchTheStockSourceOfTruth() {
        StockReportBalanceRow row = balances.find("Furadeira", category.getId(), item.getId(), 1).get(0);
        StockQuantitySummary source = unitRepository.summarizeByItemId(item.getId());
        assertEquals(5L, row.getTotalQuantity());
        assertEquals(source.getTotalQuantity(), row.getTotalQuantity());
        assertEquals(source.getAvailableQuantity(), row.getAvailableQuantity());
        assertEquals(source.getUnavailableQuantity(), row.getUnavailableQuantity());
        assertEquals(source.getMaintenanceQuantity(), row.getMaintenanceQuantity());
        assertEquals(source.getDamagedQuantity(), row.getDamagedQuantity());
        assertEquals(source.getLostQuantity(), row.getLostQuantity());
        assertEquals(2, row.getMinimumQuantity());
        assertTrue(balances.find("inexistente", -1L, -1L, -1).isEmpty());
        assertTrue(balances.find("", -99L, -1L, -1).isEmpty());
        assertTrue(balances.find("", -1L, -99L, -1).isEmpty());
    }

    @Test
    void balancesShouldIncludeItemsWithoutUnitsOrBalanceConfiguration() {
        Item emptyItem = new Item();
        emptyItem.setName("Sem unidades");
        emptyItem.setDescription("Equipamento de teste");
        emptyItem.setCategory(category);
        emptyItem.setActive(true);
        emptyItem.setCreatedBy("tester");
        itemRepository.saveAndFlush(emptyItem);
        StockReportBalanceRow row = balances.find("", -1L, emptyItem.getId(), -1).get(0);
        assertEquals(0L, row.getTotalQuantity());
        assertEquals(0L, row.getAvailableQuantity());
        assertEquals(0, row.getMinimumQuantity());
    }

    @Test
    void inactiveParentsShouldPreventAvailabilityInBothReports() {
        category.setActive(false);
        categoryRepository.saveAndFlush(category);
        StockReportBalanceRow row = balances.find("", -1L, item.getId(), -1).get(0);
        assertEquals(0L, row.getAvailableQuantity());
        assertEquals(2L, row.getUnavailableQuantity());
        assertTrue(units.find("", -1L, item.getId(), 1, "AVAILABLE", "ALL").isEmpty());
        assertEquals(2, units.find("", -1L, item.getId(), 1, "UNAVAILABLE", "GOOD").size());
        item.setActive(false);
        itemRepository.saveAndFlush(item);
        assertTrue(balances.find("", -1L, item.getId(), 1).isEmpty());
        assertEquals(1, balances.find("", -1L, item.getId(), 0).size());
    }

    @Test
    void unitReportShouldFilterRetiredUnitsAndConditions() {
        assertEquals(5, units.find("", category.getId(), item.getId(), 1, "ALL", "GOOD").size());
        List<ItemUnit> retired = units.find(retiredUnit.getAssetCode(), -1L, item.getId(), 0, "ALL", "GOOD");
        assertEquals(1, retired.size());
        assertEquals(retiredUnit.getId(), retired.get(0).getId());
        assertEquals(6, units.find("", -1L, -1L, -1, "ALL", "ALL").size());
        assertTrue(units.find("", -1L, -1L, -1, "AVAILABLE", "NEW").isEmpty());
        assertTrue(units.find("", -1L, -1L, 0, "AVAILABLE", "ALL").isEmpty());
    }

    @Test
    void movementsShouldIncludeRetiredUnitsAndInclusiveDateBoundaries() {
        List<StockMovement> january = movements.find("", category.getId(), item.getId(), "ALL",
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-02-01T00:00:00Z"), true, true);
        assertEquals(2, january.size());
        assertEquals(StockMovementType.EXIT, january.get(0).getType());
        assertEquals(retiredUnit.getId(), january.get(0).getItemUnit().getId());
        assertEquals(1, movements.find(retiredUnit.getAssetCode(), -1L, -1L, "EXIT",
                Instant.EPOCH, Instant.EPOCH, false, false).size());
        assertEquals(3, movements.find("tester", -1L, -1L, "ALL",
                Instant.EPOCH, Instant.EPOCH, false, false).size());
    }

    @Test
    void summaryShouldReturnCurrentQuantitiesAndMinimumAlert() {
        StockReportDTO summary = service.summary(new StockReportFilterDTO());
        assertEquals(1, summary.getItemCount());
        assertEquals(5, summary.getTotalQuantity());
        assertEquals(1, summary.getAvailableQuantity());
        assertEquals(1, summary.getUnavailableQuantity());
        assertEquals(1, summary.getMaintenanceQuantity());
        assertEquals(1, summary.getDamagedQuantity());
        assertEquals(1, summary.getLostQuantity());
        assertEquals(1, summary.getLowStockItemCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"balances", "low-stock", "item-units", "movements"})
    void eachReportShouldGenerateValidPdfAndExcel(String type) throws Exception {
        StockReportFileDTO pdf = service.generate(type, "pdf", new StockReportFilterDTO());
        PdfReader reader = new PdfReader(pdf.getData());
        assertTrue(reader.getNumberOfPages() > 0);
        reader.close();
        assertEquals("application/pdf", pdf.getContentType());
        StockReportFileDTO excel = service.generate(type, "xlsx", new StockReportFilterDTO());
        assertEquals('P', excel.getData()[0]);
        assertEquals('K', excel.getData()[1]);
        String strings = sharedStrings(excel.getData());
        assertTrue(strings.contains("Furadeira de relatório"));
        if (type.equals("item-units")) {
            assertTrue(strings.contains("Com baixa"));
        }
        if (type.equals("movements")) {
            assertTrue(strings.contains("Ajuste (total final)"));
            assertTrue(strings.contains("tester"));
            assertTrue(strings.contains(retiredUnit.getAssetCode()));
        }
    }

    @Test
    void lowStockShouldUseAvailableQuantityAndStrictMinimumComparison() throws Exception {
        StockBalance balance = balanceRepository.findByItemId(item.getId()).get();
        balance.setMinimumQuantity(1);
        balanceRepository.saveAndFlush(balance);
        assertEquals(0, service.summary(null).getLowStockItemCount());
        String strings = sharedStrings(service.generate("low-stock", "xlsx", null).getData());
        assertTrue(strings.contains("Nenhum registro encontrado."));
        assertFalse(strings.contains("Furadeira de relatório"));
    }

    private ItemUnit createUnit(ItemUnitStatus status, boolean active) {
        ItemUnit unit = new ItemUnit();
        unit.setItem(item);
        unit.setAssetCode("REPORT-" + (++code));
        unit.setStatus(status);
        unit.setConditionStatus(ItemUnitCondition.GOOD);
        unit.setActive(active);
        unit.setCreatedBy("tester");
        return unitRepository.saveAndFlush(unit);
    }

    private void createMovement(StockMovementType type, ItemUnit unit, String date) {
        StockMovement movement = new StockMovement();
        movement.setItem(item);
        movement.setItemUnit(unit);
        movement.setType(type);
        movement.setQuantity(1);
        movement.setReason("Operação tester");
        movement.setCreatedBy("tester");
        movementRepository.saveAndFlush(movement);
        entityManager.getEntityManager().createNativeQuery("UPDATE tb_stock_movement SET created_at = :date WHERE id = :id")
                .setParameter("date", Timestamp.from(Instant.parse(date)))
                .setParameter("id", movement.getId()).executeUpdate();
        entityManager.detach(movement);
    }

    private String sharedStrings(byte[] data) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("xl/sharedStrings.xml")) {
                    return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        fail("Arquivo Excel sem textos das células");
        return "";
    }
}
