package com.locadora_rdt_backend.modules.rentals.rental_types.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.rentals.rental_types.constants.RentalTypeConstants;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.*;
import com.locadora_rdt_backend.modules.rentals.rental_types.mapper.RentalTypeMapper;
import com.locadora_rdt_backend.modules.rentals.rental_types.model.RentalType;
import com.locadora_rdt_backend.modules.rentals.rental_types.repository.RentalTypeRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RentalTypeServiceImpl implements RentalTypeService {

    private final RentalTypeRepository repository;
    private final RentalTypeMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public RentalTypeServiceImpl(
            RentalTypeRepository repository,
            RentalTypeMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalTypeDTO> findAllPaged(String name, PageRequest pageRequest) {

        if (name == null) {
            name = "";
        }

        Page<RentalType> rentalTypes = repository.find(name.trim(), pageRequest);

        Page<RentalTypeDTO> rentalTypesDTO = rentalTypes.map(rentalType -> mapper.toDTO(rentalType));

        return rentalTypesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public RentalTypeDTO findById(Long id) {

        Optional<RentalType> rentalTypeOptional = repository.findById(id);

        if (!rentalTypeOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalTypeConstants.RENTAL_TYPE_NOT_FOUND);
        }

        RentalType rentalType = rentalTypeOptional.get();

        RentalTypeDTO rentalTypeDTO = mapper.toDTO(rentalType);

        return rentalTypeDTO;
    }

    @Override
    @Transactional
    public RentalTypeDTO insert(RentalTypeInsertDTO dto) {

        RentalType rentalType = mapper.toEntity(dto);

        rentalType.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        RentalType savedRentalType = repository.save(rentalType);

        RentalTypeDTO rentalTypeDTO = mapper.toDTO(savedRentalType);

        return rentalTypeDTO;
    }

    @Override
    @Transactional
    public RentalTypeDTO update(Long id, RentalTypeUpdateDTO dto) {

        try {

            RentalType rentalType = repository.getOne(id);

            mapper.updateEntity(rentalType, dto);

            rentalType.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            RentalType savedRentalType = repository.save(rentalType);

            RentalTypeDTO rentalTypeDTO = mapper.toDTO(savedRentalType);

            return rentalTypeDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(RentalTypeConstants.RENTAL_TYPE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(RentalTypeConstants.RENTAL_TYPE_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(RentalTypeConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(RentalTypeConstants.EMPTY_ID_LIST);
        }

        List<RentalType> rentalTypes = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (RentalType rentalType : rentalTypes) {
            existingIds.add(rentalType.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(RentalTypeConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        try {

            repository.deleteAllByIds(ids);
            repository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(RentalTypeConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException(RentalTypeConstants.RENTAL_TYPE_NOT_FOUND);
            }

        } catch (DataAccessException e) {

            throw new DatabaseException(RentalTypeConstants.CHANGE_ACTIVE_STATUS_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RentalType findEntityById(Long id) {

        Optional<RentalType> rentalTypeOptional = repository.findById(id);

        if (!rentalTypeOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalTypeConstants.RENTAL_TYPE_NOT_FOUND);
        }

        RentalType rentalType = rentalTypeOptional.get();

        return rentalType;
    }

}
