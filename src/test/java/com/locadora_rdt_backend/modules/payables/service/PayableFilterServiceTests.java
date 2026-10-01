package com.locadora_rdt_backend.modules.payables.service;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFilterDTO;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableFilterService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PayableFilterServiceTests {

    private final PayableFilterService service = new PayableFilterService();

    @Test
    void missingFiltersShouldUseDefaultStatusPeriodAndOrder() {
        PayableFilterDTO result = service.normalizeFilters(null);

        assertNull(result.getSearch());
        assertEquals("ALL", result.getStatus());
        assertEquals("DUE_DATE", result.getPeriodType());
        assertEquals("dueDate", result.getOrderBy());
        assertEquals("ASC", result.getDirection());
    }

    @Test
    void invalidFiltersShouldFallBackToDefaultsWithoutChangingTheOriginal() {
        PayableFilterDTO filters = new PayableFilterDTO();
        filters.setSearch("   ");
        filters.setStatus("inexistente");
        filters.setPeriodType("inexistente");
        filters.setOrderBy("inexistente");
        filters.setDirection("inexistente");
        filters.setSupplierId(-1L);
        filters.setEmployeeId(0L);
        filters.setPaymentMethodId(-2L);
        filters.setPaymentFrequencyId(0L);

        PayableFilterDTO result = service.normalizeFilters(filters);

        assertNull(result.getSearch());
        assertNull(result.getSupplierId());
        assertNull(result.getEmployeeId());
        assertNull(result.getPaymentMethodId());
        assertNull(result.getPaymentFrequencyId());
        assertEquals("ALL", result.getStatus());
        assertEquals("DUE_DATE", result.getPeriodType());
        assertEquals("dueDate", result.getOrderBy());
        assertEquals("ASC", result.getDirection());
        assertEquals("inexistente", filters.getStatus());
        assertEquals(-1L, filters.getSupplierId());
    }

    @Test
    void createdPeriodAliasAndPartiallyPaidStatusShouldBeRecognized() {
        PayableFilterDTO filters = new PayableFilterDTO();
        filters.setSearch(" Manutenção ");
        filters.setStatus(" partially_paid ");
        filters.setPeriodType(" created ");
        filters.setOrderBy(" createdDate ");
        filters.setDirection("desc");

        PayableFilterDTO result = service.normalizeFilters(filters);

        assertEquals("Manutenção", result.getSearch());
        assertEquals("PARTIALLY_PAID", result.getStatus());
        assertEquals("CREATED_DATE", result.getPeriodType());
        assertEquals("createdDate", result.getOrderBy());
        assertEquals("DESC", result.getDirection());
    }
}
