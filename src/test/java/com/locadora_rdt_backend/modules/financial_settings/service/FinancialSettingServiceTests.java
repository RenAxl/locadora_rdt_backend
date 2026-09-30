package com.locadora_rdt_backend.modules.financial_settings.service;

import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.mapper.FinancialSettingMapper;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import com.locadora_rdt_backend.modules.settings.financial_settings.repository.FinancialSettingRepository;
import com.locadora_rdt_backend.modules.settings.financial_settings.service.FinancialSettingServiceImpl;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FinancialSettingServiceTests {

    @Mock
    private FinancialSettingRepository repository;

    @Mock
    private FinancialSettingMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private FinancialSettingServiceImpl service;

    private FinancialSetting financialSetting;
    private FinancialSettingDTO financialSettingDTO;

    @BeforeEach
    void setUp() {
        financialSetting = new FinancialSetting();
        financialSetting.setId(1L);
        financialSetting.setSingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY);
        financialSetting.setDefaultLateFeePercent(BigDecimal.ZERO);
        financialSetting.setDefaultLateInterestPercent(BigDecimal.ZERO);
        financialSetting.setCreatedAt(Instant.parse("2026-09-01T12:00:00Z"));
        financialSetting.setCreatedBy(FinancialSettingConstants.SYSTEM_USER);

        financialSettingDTO = new FinancialSettingDTO();
        financialSettingDTO.setId(1L);
        financialSettingDTO.setDefaultLateFeePercent(BigDecimal.ZERO);
        financialSettingDTO.setDefaultLateInterestPercent(BigDecimal.ZERO);
        financialSettingDTO.setCreatedAt(financialSetting.getCreatedAt());
        financialSettingDTO.setCreatedBy(FinancialSettingConstants.SYSTEM_USER);
    }

    @Test
    void findCurrentShouldCreateDefaultWhenSettingDoesNotExist() {
        when(repository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.empty());
        when(mapper.toEntity(any(FinancialSettingUpdateDTO.class))).thenReturn(financialSetting);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(" ");
        when(repository.save(financialSetting)).thenReturn(financialSetting);
        when(mapper.toDTO(financialSetting)).thenReturn(financialSettingDTO);

        FinancialSettingDTO resultado = service.findCurrent();

        assertNotNull(resultado);
        assertEquals(financialSettingDTO, resultado);
        assertEquals(BigDecimal.ZERO, resultado.getDefaultLateFeePercent());
        assertEquals(BigDecimal.ZERO, resultado.getDefaultLateInterestPercent());
        assertEquals(financialSetting.getCreatedAt(), resultado.getCreatedAt());
        assertEquals(FinancialSettingConstants.SYSTEM_USER, financialSetting.getCreatedBy());
        verify(repository).save(financialSetting);
    }

    @Test
    void findCurrentShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findCurrent());

        verify(repository, never()).save(any(FinancialSetting.class));
    }

    @Test
    void updateShouldUpdateFinancialSetting() {
        FinancialSettingUpdateDTO updateDTO = new FinancialSettingUpdateDTO();
        updateDTO.setDefaultLateFeePercent(new BigDecimal("2.00"));
        updateDTO.setDefaultLateInterestPercent(new BigDecimal("0.50"));
        financialSettingDTO.setDefaultLateFeePercent(new BigDecimal("2.00"));
        financialSettingDTO.setDefaultLateInterestPercent(new BigDecimal("0.50"));
        financialSettingDTO.setUpdatedAt(Instant.parse("2026-09-02T12:00:00Z"));
        financialSettingDTO.setUpdatedBy("Usuário Teste");

        when(repository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.of(financialSetting));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(financialSetting)).thenReturn(financialSetting);
        when(mapper.toDTO(financialSetting)).thenReturn(financialSettingDTO);

        FinancialSettingDTO resultado = service.update(updateDTO);

        verify(mapper).updateEntity(financialSetting, updateDTO);
        verify(repository).save(financialSetting);
        assertEquals(financialSettingDTO, resultado);
        assertEquals(new BigDecimal("2.00"), resultado.getDefaultLateFeePercent());
        assertEquals(new BigDecimal("0.50"), resultado.getDefaultLateInterestPercent());
        assertEquals("Usuário Teste", financialSetting.getUpdatedBy());
        assertEquals(FinancialSettingConstants.SYSTEM_USER, financialSetting.getCreatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenRepositoryFails() {
        FinancialSettingUpdateDTO updateDTO = new FinancialSettingUpdateDTO();
        updateDTO.setDefaultLateFeePercent(new BigDecimal("2.00"));
        updateDTO.setDefaultLateInterestPercent(new BigDecimal("0.50"));

        when(repository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.of(financialSetting));
        when(repository.save(financialSetting))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.update(updateDTO));

        verify(mapper).updateEntity(financialSetting, updateDTO);
        verify(mapper, never()).toDTO(any(FinancialSetting.class));
    }

    @Test
    void findCurrentEntityShouldReturnFinancialSetting() {
        when(repository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.of(financialSetting));

        FinancialSetting resultado = service.findCurrentEntity();

        assertEquals(financialSetting, resultado);
        assertEquals(FinancialSettingConstants.DEFAULT_SINGLETON_KEY, resultado.getSingletonKey());
        verify(repository, never()).save(any(FinancialSetting.class));
    }

    @Test
    void findCurrentEntityShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findCurrentEntity());

        verify(repository, never()).save(any(FinancialSetting.class));
    }
}
