package com.locadora_rdt_backend.modules.rentals.rental.mapper;

import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalInsertDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalItemDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalItemUnitDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalStatusHistoryDTO;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItemUnit;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalStatusHistory;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RentalMapper {

    public RentalMapper() {
    }

    public RentalDTO toDTO(Rental entity) {

        RentalDTO dto = new RentalDTO();

        dto.setId(entity.getId());
        dto.setRentalNumber(entity.getRentalNumber());
        dto.setCustomerId(entity.getCustomer().getId());
        dto.setCustomerName(entity.getCustomer().getName());
        dto.setRentalTypeId(entity.getRentalType().getId());
        dto.setRentalTypeName(entity.getRentalType().getName());

        if (entity.getPaymentMethod() != null) {
            dto.setPaymentMethodId(entity.getPaymentMethod().getId());
            dto.setPaymentMethodName(entity.getPaymentMethod().getName());
        }

        dto.setStatus(entity.getStatus());
        dto.setActive(entity.getActive());
        dto.setRegistrationDate(entity.getRegistrationDate());
        dto.setRentalStartDate(entity.getRentalStartDate());
        dto.setReturnForecastDate(entity.getReturnForecastDate());
        dto.setEffectiveReturnDate(entity.getEffectiveReturnDate());
        dto.setSubtotal(entity.getSubtotal());
        dto.setDiscount(entity.getDiscount());
        dto.setShippingFee(entity.getShippingFee());
        dto.setAdditionalFee(entity.getAdditionalFee());
        dto.setLateFee(entity.getLateFee());
        dto.setDamageFee(entity.getDamageFee());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setDownPayment(entity.getDownPayment());
        dto.setRemainingAmount(entity.getRemainingAmount());
        dto.setPaid(RentalConstants.STATUS_DELIVERED.equals(entity.getStatus())
                || Boolean.TRUE.equals(entity.getPaid()));
        dto.setContractGenerated(entity.getContractGenerated());
        dto.setWhatsappSent(Boolean.TRUE.equals(entity.getWhatsappSent()));
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        List<RentalItemDTO> itemsDTO = new ArrayList<>();

        for (RentalItem item : entity.getItems()) {
            itemsDTO.add(toDTO(item));
        }

        dto.setItems(itemsDTO);

        return dto;
    }

    public Rental toEntity(RentalInsertDTO dto) {

        Rental entity = new Rental();

        entity.setRentalStartDate(dto.getRentalStartDate());
        entity.setReturnForecastDate(dto.getReturnForecastDate());
        entity.setShippingFee(dto.getShippingFee());
        entity.setAdditionalFee(dto.getAdditionalFee());
        entity.setDownPayment(dto.getDownPayment());
        entity.setActive(true);
        entity.setContractGenerated(false);
        entity.setWhatsappSent(false);

        return entity;
    }

    public void updateEntity(Rental entity, PaymentMethod paymentMethod) {

        entity.setPaymentMethod(paymentMethod);
    }

    public RentalItemDTO toDTO(RentalItem entity) {

        RentalItemDTO dto = new RentalItemDTO();

        dto.setId(entity.getId());
        dto.setItemId(entity.getItem().getId());
        dto.setItemName(entity.getItem().getName());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setDiscount(entity.getDiscount());
        dto.setAdditionalFee(entity.getAdditionalFee());
        dto.setSubtotal(entity.getSubtotal());

        return dto;
    }

    public ItemUnitDTO toDTO(ItemUnit entity) {

        ItemUnitDTO dto = new ItemUnitDTO();

        dto.setId(entity.getId());
        dto.setItemId(entity.getItem().getId());
        dto.setItemName(entity.getItem().getName());
        dto.setAssetCode(entity.getAssetCode());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }
        if (entity.getConditionStatus() != null) {
            dto.setConditionStatus(entity.getConditionStatus().name());
        }
        dto.setActive(entity.getActive());

        return dto;
    }

    public RentalItemUnitDTO toDTO(RentalItemUnit entity) {

        RentalItemUnitDTO dto = new RentalItemUnitDTO();

        dto.setId(entity.getId());
        dto.setRentalItemId(entity.getRentalItem().getId());
        dto.setItemUnitId(entity.getItemUnit().getId());
        dto.setItemName(entity.getRentalItem().getItem().getName());
        dto.setAssetCode(entity.getItemUnit().getAssetCode());
        dto.setStatus(entity.getStatus().name());
        dto.setReservedAt(entity.getReservedAt());
        dto.setDeliveredAt(entity.getDeliveredAt());
        dto.setReturnedAt(entity.getReturnedAt());

        return dto;
    }

    public RentalStatusHistoryDTO toDTO(RentalStatusHistory entity) {

        RentalStatusHistoryDTO dto = new RentalStatusHistoryDTO();

        dto.setId(entity.getId());
        dto.setPreviousStatus(entity.getPreviousStatus());
        dto.setNewStatus(entity.getNewStatus());
        dto.setReason(entity.getReason());
        dto.setChangedAt(entity.getChangedAt());
        dto.setChangedBy(entity.getChangedBy());

        return dto;
    }
}
