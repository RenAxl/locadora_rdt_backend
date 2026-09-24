package com.locadora_rdt_backend.modules.organization.positions.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.positions.constants.PositionConstants;
import com.locadora_rdt_backend.modules.organization.positions.dto.*;
import com.locadora_rdt_backend.modules.organization.positions.mapper.PositionMapper;
import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import com.locadora_rdt_backend.modules.organization.positions.repository.PositionRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PositionServiceImpl implements PositionService {

    private final PositionRepository repository;
    private final PositionMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public PositionServiceImpl(
            PositionRepository repository,
            PositionMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PositionDTO> findAllPaged(String name, PageRequest pageRequest) {

        String search = "";

        if (name != null) {
            search = name.trim();
        }

        Page<Position> positions = repository.find(search, pageRequest);

        Page<PositionDTO> positionsDTO = positions.map(position -> mapper.toDTO(position));

        return positionsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDTO findById(Long id) {

        Optional<Position> positionOptional = repository.findById(id);

        if (!positionOptional.isPresent()) {
            throw new ResourceNotFoundException(PositionConstants.POSITION_NOT_FOUND);
        }

        Position position = positionOptional.get();

        PositionDTO positionDTO = mapper.toDTO(position);

        return positionDTO;
    }

    @Override
    @Transactional
    public PositionDTO insert(PositionInsertDTO dto) {

        Position position = mapper.toEntity(dto);

        position.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        Position savedPosition = repository.save(position);

        PositionDTO positionDTO = mapper.toDTO(savedPosition);

        return positionDTO;
    }

    @Override
    @Transactional
    public PositionDTO update(Long id, PositionUpdateDTO dto) {

        Optional<Position> positionOptional = repository.findById(id);

        if (!positionOptional.isPresent()) {
            throw new ResourceNotFoundException(PositionConstants.POSITION_NOT_FOUND);
        }

        Position position = positionOptional.get();

        mapper.updateEntity(position, dto);

        position.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        Position savedPosition = repository.save(position);

        PositionDTO positionDTO = mapper.toDTO(savedPosition);

        return positionDTO;
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Optional<Position> positionOptional = repository.findById(id);

        if (!positionOptional.isPresent()) {
            throw new ResourceNotFoundException(PositionConstants.POSITION_NOT_FOUND);
        }

        Position position = positionOptional.get();

        try {
            repository.delete(position);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(PositionConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Position findEntityById(Long id) {

        Optional<Position> positionOptional = repository.findById(id);

        if (!positionOptional.isPresent()) {
            throw new ResourceNotFoundException(PositionConstants.POSITION_NOT_FOUND);
        }

        Position position = positionOptional.get();

        return position;
    }

}
