package com.locadora_rdt_backend.modules.rentals.rental.service;

import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

public interface RentalService {

    Page<RentalDTO> findAllPaged(String number, String customer, String status, Long rentalTypeId,
                                Instant dateFrom, Instant dateTo, PageRequest pageRequest);

    RentalDTO findById(Long id);

    CustomerDTO findCurrentCustomer();

    RentalDTO insert(RentalInsertDTO dto);

    RentalDTO confirm(Long id);

    RentalDTO start(Long id, RentalCheckoutDTO dto);

    ItemAvailabilityDTO findAvailability(Long itemId);

    List<ItemUnitDTO> findAvailableUnits(Long itemId);

    List<ItemUnitDTO> findItemUnits(Long itemId);

    List<RentalItemUnitDTO> findRentalUnits(Long rentalId);

    List<RentalStatusHistoryDTO> findHistory(Long rentalId);

    byte[] receipt(Long id);

    byte[] fiscalCoupon(Long id);

    void delete(Long id);
}
