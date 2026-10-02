package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFilterDTO;
import com.locadora_rdt_backend.modules.financial.receivables.model.ReceivableStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ReceivableFilterService {

    public ReceivableFilterDTO normalizeFilters(ReceivableFilterDTO filters) {
        ReceivableFilterDTO source;

        if (filters == null) {
            source = new ReceivableFilterDTO();
        } else {
            source = filters;
        }

        ReceivableFilterDTO normalized = new ReceivableFilterDTO();

        String search = source.getSearch();

        if (search == null || search.trim().isEmpty()) {
            normalized.setSearch(null);
        } else {
            normalized.setSearch(search.trim());
        }

        normalized.setStartDate(source.getStartDate());
        normalized.setEndDate(source.getEndDate());
        normalized.setStatus(normalizeStatus(source.getStatus()));
        normalized.setPeriodType(normalizePeriodType(source.getPeriodType()));
        normalized.setCustomerId(normalizeId(source.getCustomerId()));
        normalized.setPaymentMethodId(normalizeId(source.getPaymentMethodId()));
        normalized.setPaymentFrequencyId(normalizeId(source.getPaymentFrequencyId()));
        normalized.setMinimumAmount(source.getMinimumAmount());
        normalized.setMaximumAmount(source.getMaximumAmount());
        normalized.setOrderBy(normalizeOrderBy(source.getOrderBy()));
        normalized.setDirection(normalizeDirection(source.getDirection()));

        return normalized;
    }

    public Long idFilterOrDisabled(Long id) {
        if (id == null) {
            return ReceivableConstants.FILTER_ID_DISABLED;
        }

        return id;
    }

    public BigDecimal amountFilterOrDisabled(BigDecimal amount) {
        if (amount == null) {
            return ReceivableConstants.FILTER_AMOUNT_DISABLED;
        }

        return amount;
    }

    public LocalDate dateFilterOrDisabled(LocalDate date) {
        if (date == null) {
            return ReceivableConstants.FILTER_DATE_DISABLED;
        }

        return date;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return ReceivableStatus.ALL.name();
        }

        String value = status.trim().toUpperCase();

        if (ReceivableConstants.STATUS_OPEN.equals(value)) {
            return ReceivableStatus.PENDING.name();
        }

        if (ReceivableStatus.ALL.name().equals(value)) {
            return value;
        }

        if (ReceivableStatus.PENDING.name().equals(value)) {
            return value;
        }

        if (ReceivableStatus.PAID.name().equals(value)) {
            return value;
        }

        if (ReceivableStatus.OVERDUE.name().equals(value)) {
            return value;
        }

        if (ReceivableStatus.PARTIALLY_PAID.name().equals(value)) {
            return value;
        }

        if (ReceivableStatus.CANCELED.name().equals(value)) {
            return value;
        }

        return ReceivableStatus.ALL.name();
    }

    private String normalizePeriodType(String periodType) {
        if (periodType == null || periodType.trim().isEmpty()) {
            return ReceivableConstants.PERIOD_DUE_DATE;
        }

        String value = periodType.trim().toUpperCase();

        if (ReceivableConstants.PERIOD_DUE.equals(value)) {
            return ReceivableConstants.PERIOD_DUE_DATE;
        }

        if (ReceivableConstants.PERIOD_PAYMENT.equals(value)) {
            return ReceivableConstants.PERIOD_PAYMENT_DATE;
        }

        if (ReceivableConstants.PERIOD_CREATED.equals(value)) {
            return ReceivableConstants.PERIOD_CREATED_DATE;
        }

        if (ReceivableConstants.PERIOD_DUE_DATE.equals(value)) {
            return value;
        }

        if (ReceivableConstants.PERIOD_PAYMENT_DATE.equals(value)) {
            return value;
        }

        if (ReceivableConstants.PERIOD_CREATED_DATE.equals(value)) {
            return value;
        }

        return ReceivableConstants.PERIOD_DUE_DATE;
    }

    private String normalizeOrderBy(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return ReceivableConstants.ORDER_BY_DUE_DATE;
        }

        String value = orderBy.trim();
        if (ReceivableConstants.ORDER_BY_DUE_DATE.equals(value)) {
            return value;
        }

        if (ReceivableConstants.ORDER_BY_PAYMENT_DATE.equals(value)) {
            return value;
        }

        if (ReceivableConstants.ORDER_BY_CREATED_DATE.equals(value)) {
            return value;
        }

        if (ReceivableConstants.ORDER_BY_AMOUNT.equals(value)) {
            return value;
        }

        if (ReceivableConstants.ORDER_BY_DESCRIPTION.equals(value)) {
            return value;
        }

        return ReceivableConstants.ORDER_BY_DUE_DATE;
    }

    private String normalizeDirection(String direction) {
        if (ReceivableConstants.DIRECTION_DESC.equalsIgnoreCase(direction)) {
            return ReceivableConstants.DIRECTION_DESC;
        }

        return ReceivableConstants.DIRECTION_ASC;
    }

    public Long normalizeId(Long id) {
        if (id == null) {
            return null;
        }

        if (id <= 0) {
            return null;
        }

        return id;
    }

}
