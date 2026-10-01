package com.locadora_rdt_backend.modules.payment_methods.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payment_methods.constants.PaymentMethodConstants;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodInsertDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.mapper.PaymentMethodMapper;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.financial.payment_methods.repository.PaymentMethodRepository;
import com.locadora_rdt_backend.modules.financial.payment_methods.service.PaymentMethodServiceImpl;
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
import java.math.BigDecimal;
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
public class PaymentMethodServiceTests {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private PaymentMethodServiceImpl service;

    private PaymentMethod paymentMethod;
    private PaymentMethodDTO paymentMethodDTO;

    @BeforeEach
    void setUp() {
        paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setName("Pix");
        paymentMethod.setFee(new BigDecimal("2.50"));
        paymentMethod.setCreatedAt(Instant.parse("2026-09-01T12:00:00Z"));
        paymentMethod.setCreatedBy("Usuário Teste");

        paymentMethodDTO = new PaymentMethodDTO();
        paymentMethodDTO.setId(1L);
        paymentMethodDTO.setName("Pix");
        paymentMethodDTO.setFee(new BigDecimal("2.50"));
        paymentMethodDTO.setCreatedAt(paymentMethod.getCreatedAt());
        paymentMethodDTO.setCreatedBy("Usuário Teste");
    }

    @Test
    void findAllPagedShouldReturnPageOfPaymentMethods() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        Page<PaymentMethod> paymentMethods = new PageImpl<>(
                Collections.singletonList(paymentMethod), pageRequest, 1
        );

        when(repository.find("Pix", pageRequest)).thenReturn(paymentMethods);
        when(mapper.toDTO(paymentMethod)).thenReturn(paymentMethodDTO);

        Page<PaymentMethodDTO> resultado = service.findAllPaged(" Pix ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Pix", resultado.getContent().get(0).getName());
        assertEquals(new BigDecimal("2.50"), resultado.getContent().get(0).getFee());
        assertEquals(pageRequest.getSort(), resultado.getSort());
        verify(repository).find("Pix", pageRequest);
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Pix", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Pix", pageRequest));
    }

    @Test
    void findByIdShouldReturnPaymentMethod() {
        when(repository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(mapper.toDTO(paymentMethod)).thenReturn(paymentMethodDTO);

        PaymentMethodDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(new BigDecimal("2.50"), resultado.getFee());
        assertEquals(paymentMethod.getCreatedAt(), resultado.getCreatedAt());
        assertEquals("Usuário Teste", resultado.getCreatedBy());
    }

    @Test
    void findByIdShouldThrowExceptionWhenPaymentMethodDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(PaymentMethodConstants.PAYMENT_METHOD_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDTO(any(PaymentMethod.class));
    }

    @Test
    void insertShouldSavePaymentMethod() {
        PaymentMethodInsertDTO insertDTO = new PaymentMethodInsertDTO();
        insertDTO.setName("Pix");
        insertDTO.setFee(new BigDecimal("2.50"));

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(mapper.toEntity(insertDTO)).thenReturn(paymentMethod);
        when(repository.save(paymentMethod)).thenReturn(paymentMethod);
        when(mapper.toDTO(paymentMethod)).thenReturn(paymentMethodDTO);

        PaymentMethodDTO resultado = service.insert(insertDTO);

        assertEquals(paymentMethodDTO, resultado);
        verify(repository).save(paymentMethod);
        assertEquals("Usuário Teste", paymentMethod.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        PaymentMethodInsertDTO insertDTO = new PaymentMethodInsertDTO();
        insertDTO.setName("Pix");
        insertDTO.setFee(new BigDecimal("2.50"));

        when(mapper.toEntity(insertDTO)).thenReturn(paymentMethod);
        when(repository.save(paymentMethod))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdatePaymentMethod() {
        PaymentMethodUpdateDTO updateDTO = new PaymentMethodUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Cartão");
        updateDTO.setFee(new BigDecimal("2.75"));
        paymentMethodDTO.setName("Cartão");
        paymentMethodDTO.setFee(new BigDecimal("2.75"));

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.getOne(1L)).thenReturn(paymentMethod);
        when(repository.save(paymentMethod)).thenReturn(paymentMethod);
        when(mapper.toDTO(paymentMethod)).thenReturn(paymentMethodDTO);

        PaymentMethodDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(paymentMethod, updateDTO);
        verify(repository).save(paymentMethod);
        assertEquals(paymentMethodDTO, resultado);
        assertEquals(new BigDecimal("2.75"), resultado.getFee());
        assertEquals("Usuário Teste", paymentMethod.getUpdatedBy());
        assertEquals(paymentMethod.getCreatedAt(), resultado.getCreatedAt());
    }

    @Test
    void updateShouldThrowExceptionWhenPaymentMethodDoesNotExist() {
        PaymentMethodUpdateDTO updateDTO = new PaymentMethodUpdateDTO();
        updateDTO.setName("Pix");
        updateDTO.setFee(new BigDecimal("2.50"));
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));

        verify(repository, never()).save(any(PaymentMethod.class));
    }

    @Test
    void deleteShouldDeletePaymentMethod() {
        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenDatabaseIntegrityFails() {
        doThrow(new DataIntegrityViolationException("Restrição no banco"))
                .when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> service.delete(1L));

        assertEquals(PaymentMethodConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAllShouldDeleteAllPaymentMethods() {
        PaymentMethod segundaFormaPagamento = new PaymentMethod();
        segundaFormaPagamento.setId(2L);
        segundaFormaPagamento.setName("Dinheiro");

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(paymentMethod, segundaFormaPagamento));

        service.deleteAll(Arrays.asList(1L, 2L));

        verify(repository).deleteAllByIds(Arrays.asList(1L, 2L));
        verify(repository).flush();
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAll(Collections.emptyList()));

        verify(repository, never()).findAllById(any());
        verify(repository, never()).deleteAllByIds(any());
    }

    @Test
    void findEntityByIdShouldReturnPaymentMethod() {
        when(repository.findById(1L)).thenReturn(Optional.of(paymentMethod));

        PaymentMethod resultado = service.findEntityById(1L);

        assertEquals(paymentMethod, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenPaymentMethodDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }
}
