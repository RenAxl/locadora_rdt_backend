package com.locadora_rdt_backend.modules.rentals.rental_history.mapper;

import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalItemDTO;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental_history.dto.RentalHistoryDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RentalHistoryMapper {

    public RentalHistoryMapper() {
    }

    public RentalHistoryDTO toDTO(Rental entity) {

        RentalHistoryDTO dto = new RentalHistoryDTO();

        dto.setId(entity.getId());
        dto.setRentalNumber(entity.getRentalNumber());
        dto.setRentalTypeName(entity.getRentalType().getName());
        dto.setStatus(entity.getStatus());
        dto.setRegistrationDate(entity.getRegistrationDate());
        dto.setRentalStartDate(entity.getRentalStartDate());
        dto.setReturnForecastDate(entity.getReturnForecastDate());
        dto.setEffectiveReturnDate(entity.getEffectiveReturnDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setPaid(Boolean.TRUE.equals(entity.getPaid()));

        if (RentalConstants.STATUS_DELIVERED.equals(entity.getStatus())) {
            dto.setPaid(true);
        }

        List<RentalItemDTO> itemsDTO = new ArrayList<>();

        for (RentalItem item : entity.getItems()) {
            RentalItemDTO itemDTO = new RentalItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setItemId(item.getItem().getId());
            itemDTO.setItemName(item.getItem().getName());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setUnitPrice(item.getUnitPrice());
            itemDTO.setDiscount(item.getDiscount());
            itemDTO.setAdditionalFee(item.getAdditionalFee());
            itemDTO.setSubtotal(item.getSubtotal());
            itemsDTO.add(itemDTO);
        }

        dto.setItems(itemsDTO);

        return dto;
    }
}
