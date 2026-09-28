package com.locadora_rdt_backend.modules.suppliers.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.organization.suppliers.constants.SupplierConstants;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierInsertDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierImageDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierUpdateDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.mapper.SupplierMapper;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierRepository;
import com.locadora_rdt_backend.modules.organization.suppliers.service.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTests {

    @Mock
    private SupplierRepository repository;

    @Mock
    private SupplierMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private SupplierServiceImpl service;

    private Supplier supplier;
    private SupplierDTO supplierDTO;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Fornecedor Teste");
        supplier.setEmail("fornecedor@email.com");
        supplier.setCnpj("12345678000190");
        supplier.setPhoneNumber("11999999999");

        supplierDTO = new SupplierDTO();
        supplierDTO.setId(1L);
        supplierDTO.setName("Fornecedor Teste");
    }

    @Test
    void findAllPagedShouldReturnPageOfSuppliers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Supplier> suppliers = new PageImpl<>(Collections.singletonList(supplier));

        when(repository.findByNameContainingIgnoreCase("Fornecedor Teste", pageRequest)).thenReturn(suppliers);
        when(mapper.toDTO(supplier)).thenReturn(supplierDTO);

        Page<SupplierDTO> resultado = service.findAllPaged("Fornecedor Teste", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Fornecedor Teste", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.findByNameContainingIgnoreCase("Fornecedor Teste", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Fornecedor Teste", pageRequest));
    }

    @Test
    void findByIdShouldReturnSupplier() {
        SupplierDTO detailsDTO = new SupplierDTO();
        detailsDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(mapper.toDTO(supplier)).thenReturn(detailsDTO);

        SupplierDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenSupplierDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveSupplier() {
        SupplierInsertDTO insertDTO = new SupplierInsertDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(supplier);
        when(repository.save(supplier)).thenReturn(supplier);
        when(mapper.toDTO(supplier)).thenReturn(supplierDTO);

        SupplierDTO resultado = service.insert(insertDTO);

        assertEquals(supplierDTO, resultado);
        verify(repository).save(supplier);
        assertEquals("Usuário Teste", supplier.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        SupplierInsertDTO insertDTO = new SupplierInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(supplier);
        when(repository.save(supplier))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateSupplier() {
        SupplierUpdateDTO updateDTO = new SupplierUpdateDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.save(supplier)).thenReturn(supplier);
        when(mapper.toDTO(supplier)).thenReturn(supplierDTO);

        SupplierDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(supplier, updateDTO);
        verify(repository).existsByCnpjAndIdNot("12345678000190", 1L);
        verify(repository).existsByEmailIgnoreCaseAndIdNot("fornecedor@email.com", 1L);
        verify(repository).existsByPhoneNumberAndIdNot("11999999999", 1L);
        assertEquals(supplierDTO, resultado);
        assertEquals("Usuário Teste", supplier.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenSupplierDoesNotExist() {
        SupplierUpdateDTO updateDTO = new SupplierUpdateDTO();
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteSupplier() {
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        service.delete(1L);

        verify(repository).delete(supplier);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void getSupplierImageByIdShouldReturnImage() {
        byte[] imagem = new byte[]{1, 2, 3};
        supplier.setImage(imagem);
        supplier.setImageContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        SupplierImageDTO resultado = service.getSupplierImageById(1L);

        assertNotNull(resultado);
        assertArrayEquals(imagem, resultado.getImage());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getSupplierImageByIdShouldThrowExceptionWhenSupplierDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSupplierImageById(1L));
    }

    @Test
    void updateImageShouldSaveImage() {
        byte[] image = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", image);

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.updateImage(1L, file);

        assertArrayEquals(image, supplier.getImage());
        assertEquals("image/png", supplier.getImageContentType());
        assertEquals("Usuário Teste", supplier.getUpdatedBy());
        verify(repository).save(supplier);
    }

    @Test
    void updateImageShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", new byte[0]);
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        assertThrows(IllegalArgumentException.class, () -> service.updateImage(1L, file));

        verify(repository, never()).save(supplier);
    }

    @Test
    void findEntityByIdShouldReturnSupplier() {
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        Supplier resultado = service.findEntityById(1L);

        assertEquals(supplier, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenSupplierDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }

}
