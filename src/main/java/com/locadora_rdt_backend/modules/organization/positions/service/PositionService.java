package com.locadora_rdt_backend.modules.organization.positions.service;

import com.locadora_rdt_backend.modules.organization.positions.dto.PositionDTO;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionInsertDTO;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionUpdateDTO;
import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface PositionService {

    Page<PositionDTO> findAllPaged(String name, PageRequest pageRequest);

    PositionDTO findById(Long id);

    PositionDTO insert(PositionInsertDTO dto);

    PositionDTO update(Long id, PositionUpdateDTO dto);

    void delete(Long id);

    Position findEntityById(Long id);

}
