package com.locadora_rdt_backend.modules.rentals.rental_types.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.rentals.rental_types.constants.RentalTypeConstants;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeInsertDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeUpdateDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.mapper.RentalTypeMapper;
import com.locadora_rdt_backend.modules.rentals.rental_types.model.RentalType;
import com.locadora_rdt_backend.modules.rentals.rental_types.repository.RentalTypeRepository;
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

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
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
public class RentalTypeServiceTests {

    @Mock
    private RentalTypeRepository repository;

    @Mock
    private RentalTypeMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private RentalTypeServiceImpl service;

    private RentalType rentalType;
    private RentalTypeDTO rentalTypeDTO;

    @BeforeEach
    void setUp() {
        rentalType = new RentalType();
        rentalType.setId(1L);
        rentalType.setName("Mensal");
        rentalType.setType("MENSAL");
        rentalType.setDays(30);
        rentalType.setActive(true);

        rentalTypeDTO = new RentalTypeDTO();
        rentalTypeDTO.setId(1L);
        rentalTypeDTO.setName("Mensal");
        rentalTypeDTO.setType("MENSAL");
        rentalTypeDTO.setDays(30);
        rentalTypeDTO.setActive(true);
    }

    @Test
    void findAllPagedShouldReturnPageOfRentalTypes() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<RentalType> rentalTypes = new PageImpl<>(Collections.singletonList(rentalType));

        when(repository.find("Mensal", pageRequest)).thenReturn(rentalTypes);
        when(mapper.toDTO(rentalType)).thenReturn(rentalTypeDTO);

        Page<RentalTypeDTO> resultado = service.findAllPaged(" Mensal ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mensal", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Mensal", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Mensal", pageRequest));
    }

    @Test
    void findByIdShouldReturnRentalType() {
        when(repository.findById(1L)).thenReturn(Optional.of(rentalType));
        when(mapper.toDTO(rentalType)).thenReturn(rentalTypeDTO);

        RentalTypeDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(rentalTypeDTO, resultado);
        verify(mapper).toDTO(rentalType);
    }

    @Test
    void findByIdShouldThrowExceptionWhenRentalTypeDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveRentalType() {
        RentalTypeInsertDTO insertDTO = new RentalTypeInsertDTO();
        insertDTO.setName("Mensal");
        insertDTO.setType("MENSAL");
        insertDTO.setDays(30);

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(rentalType);
        when(repository.save(rentalType)).thenReturn(rentalType);
        when(mapper.toDTO(rentalType)).thenReturn(rentalTypeDTO);

        RentalTypeDTO resultado = service.insert(insertDTO);

        assertEquals(rentalTypeDTO, resultado);
        verify(repository).save(rentalType);
        assertEquals("Usuário Teste", rentalType.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        RentalTypeInsertDTO insertDTO = new RentalTypeInsertDTO();
        insertDTO.setName("Mensal");
        insertDTO.setType("MENSAL");
        insertDTO.setDays(30);

        when(mapper.toEntity(insertDTO)).thenReturn(rentalType);
        when(repository.save(rentalType))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateRentalType() {
        RentalTypeUpdateDTO updateDTO = new RentalTypeUpdateDTO();
        updateDTO.setName("Semanal");
        updateDTO.setType("SEMANAL");
        updateDTO.setDays(7);

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.getOne(1L)).thenReturn(rentalType);
        when(repository.save(rentalType)).thenReturn(rentalType);
        when(mapper.toDTO(rentalType)).thenReturn(rentalTypeDTO);

        RentalTypeDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(rentalType, updateDTO);
        assertEquals(rentalTypeDTO, resultado);
        assertEquals("Usuário Teste", rentalType.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenRentalTypeDoesNotExist() {
        RentalTypeUpdateDTO updateDTO = new RentalTypeUpdateDTO();
        updateDTO.setName("Semanal");
        updateDTO.setType("SEMANAL");
        updateDTO.setDays(7);

        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteRentalType() {
        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenDatabaseIntegrityIsViolated() {
        doThrow(new DataIntegrityViolationException("Tipo de locação em uso"))
                .when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> service.delete(1L));

        assertEquals(RentalTypeConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAllShouldDeleteAllRentalTypes() {
        RentalType segundoTipoLocacao = new RentalType();
        segundoTipoLocacao.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(rentalType, segundoTipoLocacao));

        service.deleteAll(Arrays.asList(1L, 2L));

        verify(repository).deleteAllByIds(Arrays.asList(1L, 2L));
        verify(repository).flush();
    }

    @Test
    void deleteAllShouldThrowExceptionWhenOneOrMoreIdsDoNotExist() {
        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Collections.singletonList(rentalType));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.deleteAll(Arrays.asList(1L, 2L)));

        assertEquals(RentalTypeConstants.ONE_OR_MORE_IDS_NOT_FOUND, exception.getMessage());
        verify(repository, never()).deleteAllByIds(any());
        verify(repository, never()).flush();
    }

    @Test
    void changeActiveStatusShouldChangeStatus() {
        when(repository.updateActiveById(1L, false)).thenReturn(1);

        service.changeActiveStatus(1L, false);

        verify(repository).updateActiveById(1L, false);
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenDatabaseFails() {
        when(repository.updateActiveById(1L, false))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DatabaseException.class,
                () -> service.changeActiveStatus(1L, false));
    }

    @Test
    void findEntityByIdShouldReturnRentalType() {
        when(repository.findById(1L)).thenReturn(Optional.of(rentalType));

        RentalType resultado = service.findEntityById(1L);

        assertEquals(rentalType, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenRentalTypeDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }
}
