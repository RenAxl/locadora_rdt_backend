package com.locadora_rdt_backend.modules.financial.receivables.mapper;

import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableInsertDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableUpdateDTO;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ReceivableMapper {

    public ReceivableMapper() {
    }

    public ReceivableDTO toDTO(Receivable entity) {

        ReceivableDTO dto = new ReceivableDTO();

        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setAmount(entity.getAmount());
        dto.setOriginalAmount(entity.getAmount());
        dto.setDueDate(entity.getDueDate());
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setNote(entity.getNote());
        dto.setFileName(entity.getFileName());
        dto.setPaid(entity.getPaid());
        dto.setRemainingBalance(entity.getRemainingBalance());
        dto.setLateFee(entity.getLateFee());
        dto.setLateInterest(entity.getLateInterest());
        dto.setDiscount(entity.getDiscount());
        dto.setFee(entity.getFee());
        dto.setSubtotal(entity.getSubtotal());
        dto.setResidual(entity.getResidual());
        dto.setCanceled(entity.getCanceled());
        if (entity.getParentReceivable() != null) {
            dto.setParentReceivableId(entity.getParentReceivable().getId());
            dto.setOriginalAmount(entity.getParentReceivable().getAmount());
        }

        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }

        if (entity.getPaymentMethod() != null) {
            dto.setPaymentMethodId(entity.getPaymentMethod().getId());
            dto.setPaymentMethodName(entity.getPaymentMethod().getName());
        }

        if (entity.getPaymentFrequency() != null) {
            dto.setPaymentFrequencyId(entity.getPaymentFrequency().getId());
            dto.setPaymentFrequency(entity.getPaymentFrequency().getFrequency());
        }

        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByName(entity.getCreatedBy().getName());
        }

        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedById(entity.getUpdatedBy().getId());
            dto.setUpdatedByName(entity.getUpdatedBy().getName());
        }

        if (entity.getPaidBy() != null) {
            dto.setPaidById(entity.getPaidBy().getId());
            dto.setPaidByName(entity.getPaidBy().getName());
        }

        return dto;
    }

    public Receivable toEntity(ReceivableInsertDTO dto) {

        Receivable entity = new Receivable();

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            entity.setDescription(null);
        } else {
            entity.setDescription(dto.getDescription().trim());
        }

        if (dto.getNote() == null || dto.getNote().trim().isEmpty()) {
            entity.setNote(null);
        } else {
            entity.setNote(dto.getNote().trim());
        }

        if (dto.getFileName() == null || dto.getFileName().trim().isEmpty()) {
            entity.setFileName(null);
        } else {
            entity.setFileName(dto.getFileName().trim());
        }

        entity.setAmount(dto.getAmount());
        entity.setDueDate(dto.getDueDate());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setPaid(false);
        entity.setRemainingBalance(dto.getAmount());

        if (dto.getPaymentDate() != null) {
            entity.setPaid(true);
            entity.setRemainingBalance(BigDecimal.ZERO);
        }

        return entity;
    }

    public void updateEntity(Receivable entity, ReceivableUpdateDTO dto) {

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            entity.setDescription(null);
        } else {
            entity.setDescription(dto.getDescription().trim());
        }

        if (dto.getNote() == null || dto.getNote().trim().isEmpty()) {
            entity.setNote(null);
        } else {
            entity.setNote(dto.getNote().trim());
        }

        if (dto.getFileName() == null || dto.getFileName().trim().isEmpty()) {
            entity.setFileName(null);
        } else {
            entity.setFileName(dto.getFileName().trim());
        }

        entity.setAmount(dto.getAmount());
        entity.setDueDate(dto.getDueDate());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setPaid(false);
        entity.setRemainingBalance(dto.getAmount());

        if (dto.getPaymentDate() != null) {
            entity.setPaid(true);
            entity.setRemainingBalance(BigDecimal.ZERO);
        }
    }
}
