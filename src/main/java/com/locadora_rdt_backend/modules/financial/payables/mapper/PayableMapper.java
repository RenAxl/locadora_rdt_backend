package com.locadora_rdt_backend.modules.financial.payables.mapper;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableInsertDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayableMapper {

    public PayableMapper() {
    }

    public PayableDTO toDTO(Payable entity) {

        PayableDTO dto = new PayableDTO();

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
        if (entity.getParentPayable() != null) {
            dto.setParentPayableId(entity.getParentPayable().getId());
            dto.setOriginalAmount(entity.getParentPayable().getAmount());
        }

        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
        }

        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
            dto.setEmployeeName(entity.getEmployee().getName());
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

    public Payable toEntity(PayableInsertDTO dto) {

        Payable entity = new Payable();

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

    public void updateEntity(Payable entity, PayableUpdateDTO dto) {

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
