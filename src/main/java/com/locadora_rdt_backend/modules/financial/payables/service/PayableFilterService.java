package com.locadora_rdt_backend.modules.financial.payables.service;

import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFilterDTO;
import com.locadora_rdt_backend.modules.financial.payables.model.PayableStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PayableFilterService {

    public PayableFilterDTO normalizeFilters(PayableFilterDTO filters) {
        PayableFilterDTO source;

        if (filters == null) {
            source = new PayableFilterDTO();
        } else {
            source = filters;
        }

        PayableFilterDTO normalized = new PayableFilterDTO();

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
        normalized.setSupplierId(normalizeId(source.getSupplierId()));
        normalized.setEmployeeId(normalizeId(source.getEmployeeId()));
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
            return PayableConstants.FILTER_ID_DISABLED;
        }

        return id;
    }

    public BigDecimal amountFilterOrDisabled(BigDecimal amount) {
        if (amount == null) {
            return PayableConstants.FILTER_AMOUNT_DISABLED;
        }

        return amount;
    }

    public LocalDate dateFilterOrDisabled(LocalDate date) {
        if (date == null) {
            return PayableConstants.FILTER_DATE_DISABLED;
        }

        return date;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return PayableStatus.ALL.name();
        }

        String value = status.trim().toUpperCase();

        if (PayableConstants.STATUS_OPEN.equals(value)) {
            return PayableStatus.PENDING.name();
        }

        if (PayableStatus.ALL.name().equals(value)) {
            return value;
        }

        if (PayableStatus.PENDING.name().equals(value)) {
            return value;
        }

        if (PayableStatus.PAID.name().equals(value)) {
            return value;
        }

        if (PayableStatus.OVERDUE.name().equals(value)) {
            return value;
        }

        if (PayableStatus.PARTIALLY_PAID.name().equals(value)) {
            return value;
        }

        if (PayableStatus.CANCELED.name().equals(value)) {
            return value;
        }

        return PayableStatus.ALL.name();
    }

    private String normalizePeriodType(String periodType) {
        if (periodType == null || periodType.trim().isEmpty()) {
            return PayableConstants.PERIOD_DUE_DATE;
        }

        String value = periodType.trim().toUpperCase();

        if (PayableConstants.PERIOD_DUE.equals(value)) {
            return PayableConstants.PERIOD_DUE_DATE;
        }

        if (PayableConstants.PERIOD_PAYMENT.equals(value)) {
            return PayableConstants.PERIOD_PAYMENT_DATE;
        }

        if (PayableConstants.PERIOD_CREATED.equals(value)) {
            return PayableConstants.PERIOD_CREATED_DATE;
        }

        if (PayableConstants.PERIOD_DUE_DATE.equals(value)) {
            return value;
        }

        if (PayableConstants.PERIOD_PAYMENT_DATE.equals(value)) {
            return value;
        }

        if (PayableConstants.PERIOD_CREATED_DATE.equals(value)) {
            return value;
        }

        return PayableConstants.PERIOD_DUE_DATE;
    }

    private String normalizeOrderBy(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return PayableConstants.ORDER_BY_DUE_DATE;
        }

        String value = orderBy.trim();
        if (PayableConstants.ORDER_BY_DUE_DATE.equals(value)) {
            return value;
        }

        if (PayableConstants.ORDER_BY_PAYMENT_DATE.equals(value)) {
            return value;
        }

        if (PayableConstants.ORDER_BY_CREATED_DATE.equals(value)) {
            return value;
        }

        if (PayableConstants.ORDER_BY_AMOUNT.equals(value)) {
            return value;
        }

        if (PayableConstants.ORDER_BY_DESCRIPTION.equals(value)) {
            return value;
        }

        return PayableConstants.ORDER_BY_DUE_DATE;
    }

    private String normalizeDirection(String direction) {
        if (PayableConstants.DIRECTION_DESC.equalsIgnoreCase(direction)) {
            return PayableConstants.DIRECTION_DESC;
        }

        return PayableConstants.DIRECTION_ASC;
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
