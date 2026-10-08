package com.locadora_rdt_backend.modules.rentals.rental_types.service;

import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeInsertDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeUpdateDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.model.RentalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface RentalTypeService {

    Page<RentalTypeDTO> findAllPaged(String name, PageRequest pageRequest);

    RentalTypeDTO findById(Long id);

    RentalTypeDTO insert(RentalTypeInsertDTO dto);

    RentalTypeDTO update(Long id, RentalTypeUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    RentalType findEntityById(Long id);

}
