package com.locadora_rdt_backend.modules.system_settings.service;

import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.mapper.SystemSettingMapper;
import com.locadora_rdt_backend.modules.settings.system_settings.model.Address;
import com.locadora_rdt_backend.modules.settings.system_settings.model.SystemSetting;
import com.locadora_rdt_backend.modules.settings.system_settings.repository.SystemSettingRepository;
import com.locadora_rdt_backend.modules.settings.system_settings.service.SystemSettingServiceImpl;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SystemSettingServiceTests {

    @Mock
    private SystemSettingRepository repository;

    @Mock
    private SystemSettingMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private SystemSettingServiceImpl service;

    private SystemSetting systemSetting;
    private SystemSettingDTO systemSettingDTO;

    @BeforeEach
    void setUp() {
        Address address = new Address();
        address.setStreet("Rua Central");
        address.setNumber("100");
        address.setNeighborhood("Centro");
        address.setCity("Sao Paulo");
        address.setState("SP");
        address.setZipCode("01001-000");

        systemSetting = new SystemSetting();
        systemSetting.setId(1L);
        systemSetting.setSingletonKey("DEFAULT");
        systemSetting.setCompanyName("Locadora RDT");
        systemSetting.setAddress(address);
        systemSetting.setCreatedBy("Maria");

        systemSettingDTO = new SystemSettingDTO();
        systemSettingDTO.setId(1L);
        systemSettingDTO.setCompanyName("Locadora RDT");
        systemSettingDTO.setAddress(address);
    }

    @Test
    void findCurrentShouldReturnSystemSetting() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.of(systemSetting));
        when(mapper.toDTO(systemSetting)).thenReturn(systemSettingDTO);

        SystemSettingDTO resultado = service.findCurrent();

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Locadora RDT", resultado.getCompanyName());
        assertEquals(systemSetting.getAddress(), resultado.getAddress());
        verify(repository, never()).save(any());
    }

    @Test
    void findCurrentShouldCreateDefaultWhenSystemSettingDoesNotExist() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Maria");
        when(repository.save(any(SystemSetting.class))).thenReturn(systemSetting);
        when(mapper.toDTO(systemSetting)).thenReturn(systemSettingDTO);

        SystemSettingDTO resultado = service.findCurrent();

        assertEquals(systemSettingDTO, resultado);
        verify(authenticationFacade).getAuthenticatedUsername();
        verify(repository).save(any(SystemSetting.class));
        verify(mapper).toDTO(systemSetting);
    }

    @Test
    void findCurrentShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findBySingletonKey("DEFAULT"))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findCurrent());

        verify(repository, never()).save(any());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void findCurrentShouldThrowExceptionWhenDefaultCannotBeSaved() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(repository.save(any(SystemSetting.class)))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findCurrent());

        verify(mapper, never()).toDTO(any());
    }

    @Test
    void updateShouldUpdateSystemSetting() {
        SystemSettingUpdateDTO updateDTO = new SystemSettingUpdateDTO();
        updateDTO.setCompanyName("Locadora Atualizada");
        updateDTO.setAddress(systemSetting.getAddress());

        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.of(systemSetting));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Joao");
        when(repository.save(systemSetting)).thenReturn(systemSetting);
        when(mapper.toDTO(systemSetting)).thenReturn(systemSettingDTO);

        SystemSettingDTO resultado = service.update(updateDTO);

        verify(mapper).updateEntity(systemSetting, updateDTO);
        verify(repository).save(systemSetting);
        assertEquals(systemSettingDTO, resultado);
        assertEquals("Joao", systemSetting.getUpdatedBy());
        assertEquals("Maria", systemSetting.getCreatedBy());
        assertEquals("DEFAULT", systemSetting.getSingletonKey());
    }

    @Test
    void updateShouldCreateDefaultWhenSystemSettingDoesNotExist() {
        SystemSettingUpdateDTO updateDTO = new SystemSettingUpdateDTO();
        updateDTO.setCompanyName("Locadora Atualizada");
        updateDTO.setAddress(systemSetting.getAddress());

        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Joao");
        when(repository.save(any(SystemSetting.class))).thenReturn(systemSetting);
        when(mapper.toDTO(systemSetting)).thenReturn(systemSettingDTO);

        SystemSettingDTO resultado = service.update(updateDTO);

        verify(mapper).updateEntity(systemSetting, updateDTO);
        verify(repository).save(systemSetting);
        assertEquals(systemSettingDTO, resultado);
        assertEquals("Joao", systemSetting.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenRepositoryFails() {
        SystemSettingUpdateDTO updateDTO = new SystemSettingUpdateDTO();

        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.of(systemSetting));
        when(repository.save(systemSetting))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.update(updateDTO));

        verify(mapper).updateEntity(systemSetting, updateDTO);
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void updateShouldThrowExceptionWhenCurrentSystemSettingCannotBeLoaded() {
        SystemSettingUpdateDTO updateDTO = new SystemSettingUpdateDTO();

        when(repository.findBySingletonKey("DEFAULT"))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.update(updateDTO));

        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void findCurrentEntityShouldReturnSystemSetting() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.of(systemSetting));

        SystemSetting resultado = service.findCurrentEntity();

        assertNotNull(resultado);
        assertEquals(systemSetting, resultado);
        verify(repository, never()).save(any());
    }

    @Test
    void findCurrentEntityShouldCreateDefaultWhenSystemSettingDoesNotExist() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Maria");
        when(repository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        SystemSetting resultado = service.findCurrentEntity();

        assertEquals(systemSetting, resultado);
        verify(repository).save(any(SystemSetting.class));
    }

    @Test
    void findCurrentEntityShouldCreateDefaultWhenUsernameIsNull() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(null);
        when(repository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        SystemSetting resultado = service.findCurrentEntity();

        assertEquals(systemSetting, resultado);
        verify(repository).save(any(SystemSetting.class));
    }

    @Test
    void findCurrentEntityShouldCreateDefaultWhenUsernameIsBlank() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("   ");
        when(repository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        SystemSetting resultado = service.findCurrentEntity();

        assertEquals(systemSetting, resultado);
        verify(repository).save(any(SystemSetting.class));
    }

    @Test
    void findCurrentEntityShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findBySingletonKey("DEFAULT"))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findCurrentEntity());

        verify(repository, never()).save(any());
    }

    @Test
    void findCurrentEntityShouldThrowExceptionWhenDefaultCannotBeSaved() {
        when(repository.findBySingletonKey("DEFAULT")).thenReturn(Optional.empty());
        when(repository.save(any(SystemSetting.class)))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findCurrentEntity());
    }
}
