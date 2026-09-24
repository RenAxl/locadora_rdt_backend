package com.locadora_rdt_backend.modules.positions.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.positions.constants.PositionConstants;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionDTO;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionInsertDTO;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionUpdateDTO;
import com.locadora_rdt_backend.modules.organization.positions.mapper.PositionMapper;
import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import com.locadora_rdt_backend.modules.organization.positions.repository.PositionRepository;
import com.locadora_rdt_backend.modules.organization.positions.service.PositionServiceImpl;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PositionServiceTests {

    @Mock
    private PositionRepository repository;

    @Mock
    private PositionMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private PositionServiceImpl service;

    private Position position;
    private PositionDTO positionDTO;

    @BeforeEach
    void setUp() {
        position = new Position();
        position.setId(1L);
        position.setName("Motorista");

        positionDTO = new PositionDTO();
        positionDTO.setId(1L);
        positionDTO.setName("Motorista");
    }

    @Test
    void findAllPagedShouldReturnPageOfPositions() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Position> positions = new PageImpl<>(Collections.singletonList(position));

        when(repository.find("Motorista", pageRequest)).thenReturn(positions);
        when(mapper.toDTO(position)).thenReturn(positionDTO);

        Page<PositionDTO> resultado = service.findAllPaged("Motorista", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Motorista", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Motorista", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Motorista", pageRequest));
    }

    @Test
    void findByIdShouldReturnPosition() {
        PositionDTO detailsDTO = new PositionDTO();
        detailsDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(position));
        when(mapper.toDTO(position)).thenReturn(detailsDTO);

        PositionDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenPositionDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSavePosition() {
        PositionInsertDTO insertDTO = new PositionInsertDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(position);
        when(repository.save(position)).thenReturn(position);
        when(mapper.toDTO(position)).thenReturn(positionDTO);

        PositionDTO resultado = service.insert(insertDTO);

        assertEquals(positionDTO, resultado);
        verify(repository).save(position);
        assertEquals("Usuário Teste", position.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        PositionInsertDTO insertDTO = new PositionInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(position);
        when(repository.save(position))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdatePosition() {
        PositionUpdateDTO updateDTO = new PositionUpdateDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.findById(1L)).thenReturn(Optional.of(position));
        when(repository.save(position)).thenReturn(position);
        when(mapper.toDTO(position)).thenReturn(positionDTO);

        PositionDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(position, updateDTO);
        assertEquals(positionDTO, resultado);
        assertEquals("Usuário Teste", position.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenPositionDoesNotExist() {
        PositionUpdateDTO updateDTO = new PositionUpdateDTO();
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeletePosition() {
        when(repository.findById(1L)).thenReturn(Optional.of(position));

        service.delete(1L);

        verify(repository).delete(position);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));

        verify(repository, never()).delete(any(Position.class));
        verify(repository, never()).flush();
    }

    @Test
    void findEntityByIdShouldReturnPosition() {
        when(repository.findById(1L)).thenReturn(Optional.of(position));

        Position resultado = service.findEntityById(1L);

        assertEquals(position, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenPositionDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }

    @Test
    void findAllPagedShouldTrimName() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Position> positions = new PageImpl<>(Collections.singletonList(position));

        when(repository.find("Motorista", pageRequest)).thenReturn(positions);
        when(mapper.toDTO(position)).thenReturn(positionDTO);

        Page<PositionDTO> resultado = service.findAllPaged(" Motorista ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        verify(repository).find("Motorista", pageRequest);
    }

    @Test
    void findAllPagedShouldUseEmptySearchWhenNameIsNull() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("", pageRequest)).thenReturn(Page.empty());

        Page<PositionDTO> resultado = service.findAllPaged(null, pageRequest);

        assertEquals(0, resultado.getTotalElements());
        verify(repository).find("", pageRequest);
    }

    @Test
    void updateShouldThrowExceptionWhenRepositoryFails() {
        PositionUpdateDTO updateDTO = new PositionUpdateDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(position));
        when(repository.save(position))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldThrowExceptionWhenPositionIsInUse() {
        when(repository.findById(1L)).thenReturn(Optional.of(position));
        doThrow(new DataIntegrityViolationException("Cargo vinculado"))
                .when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.delete(1L));

        assertEquals(PositionConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(repository).delete(position);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenDeleteViolatesIntegrity() {
        when(repository.findById(1L)).thenReturn(Optional.of(position));
        doThrow(new DataIntegrityViolationException("Cargo vinculado"))
                .when(repository).delete(position);

        assertThrows(DatabaseException.class, () -> service.delete(1L));

        verify(repository, never()).flush();
    }
}
