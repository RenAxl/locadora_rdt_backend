package com.locadora_rdt_backend.modules.financial.payment_frequencies.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.constants.PaymentFrequencyConstants;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyInsertDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.mapper.PaymentFrequencyMapper;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.repository.PaymentFrequencyRepository;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.service.PaymentFrequencyServiceImpl;
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
import org.springframework.data.domain.Sort;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
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
public class PaymentFrequencyServiceTests {

    @Mock
    private PaymentFrequencyRepository repository;

    @Mock
    private PaymentFrequencyMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private PaymentFrequencyServiceImpl service;

    private PaymentFrequency paymentFrequency;
    private PaymentFrequencyDTO paymentFrequencyDTO;

    @BeforeEach
    void setUp() {
        paymentFrequency = new PaymentFrequency();
        paymentFrequency.setId(1L);
        paymentFrequency.setFrequency("Mensal");
        paymentFrequency.setDays(30);
        paymentFrequency.setCreatedAt(Instant.parse("2026-09-01T12:00:00Z"));
        paymentFrequency.setCreatedBy("Usuário Original");

        paymentFrequencyDTO = new PaymentFrequencyDTO();
        paymentFrequencyDTO.setId(1L);
        paymentFrequencyDTO.setFrequency("Mensal");
        paymentFrequencyDTO.setDays(30);
        paymentFrequencyDTO.setCreatedAt(Instant.parse("2026-09-01T12:00:00Z"));
        paymentFrequencyDTO.setCreatedBy("Usuário Original");
    }

    @Test
    void findAllPagedShouldReturnPageOfPaymentFrequencies() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "frequency");
        Page<PaymentFrequency> paymentFrequencies = new PageImpl<>(
                Collections.singletonList(paymentFrequency), pageRequest, 1
        );

        when(repository.find("Mensal", pageRequest)).thenReturn(paymentFrequencies);
        when(mapper.toDTO(paymentFrequency)).thenReturn(paymentFrequencyDTO);

        Page<PaymentFrequencyDTO> resultado = service.findAllPaged("  Mensal  ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mensal", resultado.getContent().get(0).getFrequency());
        assertEquals(30, resultado.getContent().get(0).getDays());
        assertEquals(pageRequest, resultado.getPageable());
        verify(repository).find("Mensal", pageRequest);
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
    void findByIdShouldReturnPaymentFrequency() {
        when(repository.findById(1L)).thenReturn(Optional.of(paymentFrequency));
        when(mapper.toDTO(paymentFrequency)).thenReturn(paymentFrequencyDTO);

        PaymentFrequencyDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Mensal", resultado.getFrequency());
        assertEquals(30, resultado.getDays());
        assertEquals(paymentFrequency.getCreatedAt(), resultado.getCreatedAt());
        assertEquals("Usuário Original", resultado.getCreatedBy());
    }

    @Test
    void findByIdShouldThrowExceptionWhenPaymentFrequencyDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSavePaymentFrequency() {
        PaymentFrequencyInsertDTO insertDTO = new PaymentFrequencyInsertDTO();
        insertDTO.setFrequency("Mensal");
        insertDTO.setDays(30);
        paymentFrequencyDTO.setCreatedBy("Usuário Teste");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(mapper.toEntity(insertDTO)).thenReturn(paymentFrequency);
        when(repository.save(paymentFrequency)).thenReturn(paymentFrequency);
        when(mapper.toDTO(paymentFrequency)).thenReturn(paymentFrequencyDTO);

        PaymentFrequencyDTO resultado = service.insert(insertDTO);

        assertEquals(paymentFrequencyDTO, resultado);
        verify(repository).save(paymentFrequency);
        assertEquals("Usuário Teste", paymentFrequency.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenFrequencyAlreadyExists() {
        PaymentFrequencyInsertDTO insertDTO = new PaymentFrequencyInsertDTO();
        insertDTO.setFrequency("Mensal");
        insertDTO.setDays(30);

        when(mapper.toEntity(insertDTO)).thenReturn(paymentFrequency);
        when(repository.save(paymentFrequency))
                .thenThrow(new DataIntegrityViolationException("Frequência já existe"));

        assertThrows(DataIntegrityViolationException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdatePaymentFrequency() {
        PaymentFrequencyUpdateDTO updateDTO = new PaymentFrequencyUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setFrequency("Mensal");
        updateDTO.setDays(30);
        paymentFrequencyDTO.setUpdatedBy("Usuário Teste");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.getOne(1L)).thenReturn(paymentFrequency);
        when(repository.save(paymentFrequency)).thenReturn(paymentFrequency);
        when(mapper.toDTO(paymentFrequency)).thenReturn(paymentFrequencyDTO);

        PaymentFrequencyDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(paymentFrequency, updateDTO);
        assertEquals(paymentFrequencyDTO, resultado);
        assertEquals("Usuário Teste", paymentFrequency.getUpdatedBy());
        assertEquals("Usuário Original", paymentFrequency.getCreatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenPaymentFrequencyDoesNotExist() {
        PaymentFrequencyUpdateDTO updateDTO = new PaymentFrequencyUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));

        verify(repository, never()).save(any());
    }

    @Test
    void deleteShouldDeletePaymentFrequency() {
        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenDatabaseIntegrityIsViolated() {
        doThrow(new DataIntegrityViolationException("Frequência em uso")).when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> service.delete(1L));

        assertEquals(PaymentFrequencyConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAllShouldDeleteAllPaymentFrequencies() {
        PaymentFrequency segundaFrequencia = new PaymentFrequency();
        segundaFrequencia.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(paymentFrequency, segundaFrequencia));

        service.deleteAll(Arrays.asList(1L, 2L));

        verify(repository).deleteAllByIds(Arrays.asList(1L, 2L));
        verify(repository).flush();
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAll(Collections.emptyList()));

        verify(repository, never()).deleteAllByIds(any());
        verify(repository, never()).flush();
    }

    @Test
    void findEntityByIdShouldReturnPaymentFrequency() {
        when(repository.findById(1L)).thenReturn(Optional.of(paymentFrequency));

        PaymentFrequency resultado = service.findEntityById(1L);

        assertEquals(paymentFrequency, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenPaymentFrequencyDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }
}
