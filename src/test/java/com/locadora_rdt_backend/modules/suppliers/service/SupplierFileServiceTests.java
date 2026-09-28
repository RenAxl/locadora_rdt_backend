package com.locadora_rdt_backend.modules.suppliers.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.organization.suppliers.constants.SupplierConstants;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileViewDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.mapper.SupplierFileMapper;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import com.locadora_rdt_backend.modules.organization.suppliers.model.SupplierFile;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierFileRepository;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierRepository;
import com.locadora_rdt_backend.modules.organization.suppliers.service.SupplierFileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierFileServiceTests {

    @Mock
    private SupplierFileRepository repository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierFileMapper mapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private SupplierFileServiceImpl service;

    private Supplier supplier;
    private SupplierFile supplierFile;
    private SupplierFileDTO supplierFileDTO;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Fornecedor Teste");

        supplierFile = new SupplierFile();
        supplierFile.setId(2L);
        supplierFile.setSupplier(supplier);
        supplierFile.setName("Contrato");
        supplierFile.setOriginalFileName("contrato.pdf");
        supplierFile.setContentType("application/pdf");
        supplierFile.setData(new byte[]{1, 2, 3});

        supplierFileDTO = new SupplierFileDTO();
        supplierFileDTO.setId(2L);
        supplierFileDTO.setName("Contrato");
        supplierFileDTO.setSupplierId(1L);
    }

    @Test
    void uploadShouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.save(any(SupplierFile.class))).thenReturn(supplierFile);
        when(mapper.toDTO(supplierFile)).thenReturn(supplierFileDTO);

        SupplierFileDTO resultado = service.upload(1L, "Contrato", file);

        assertEquals(supplierFileDTO, resultado);
        assertEquals(1L, resultado.getSupplierId());
        verify(repository).save(any(SupplierFile.class));
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[0]
        );
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void findAllBySupplierShouldReturnFiles() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.findBySupplierIdOrderByIdDesc(1L))
                .thenReturn(Collections.singletonList(supplierFile));
        when(mapper.toDTO(supplierFile)).thenReturn(supplierFileDTO);

        List<SupplierFileDTO> resultado = service.findAllBySupplier(1L);

        assertEquals(1, resultado.size());
        assertEquals("Contrato", resultado.get(0).getName());
    }

    @Test
    void findAllBySupplierShouldThrowExceptionWhenSupplierDoesNotExist() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findAllBySupplier(1L));

        verify(repository, never()).findBySupplierIdOrderByIdDesc(1L);
    }

    @Test
    void downloadShouldReturnFile() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.findById(2L)).thenReturn(Optional.of(supplierFile));

        SupplierFileViewDTO resultado = service.download(1L, 2L);

        assertNotNull(resultado);
        assertEquals("contrato.pdf", resultado.getFileName());
        assertEquals("application/pdf", resultado.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, resultado.getData());
    }

    @Test
    void downloadShouldThrowExceptionWhenFileDoesNotBelongToSupplier() {
        Supplier outroFornecedor = new Supplier();
        outroFornecedor.setId(3L);
        supplierFile.setSupplier(outroFornecedor);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.findById(2L)).thenReturn(Optional.of(supplierFile));

        assertThrows(ResourceNotFoundException.class, () -> service.download(1L, 2L));
    }

    @Test
    void deleteShouldDeleteFile() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.findById(2L)).thenReturn(Optional.of(supplierFile));

        service.delete(1L, 2L);

        verify(repository).delete(supplierFile);
    }

    @Test
    void deleteShouldThrowExceptionWhenFileDoesNotExist() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 2L));

        verify(repository, never()).delete(any(SupplierFile.class));
    }

    @Test
    void uploadShouldThrowExceptionWhenNameIsBlank() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        assertThrows(FileException.class, () -> service.upload(1L, " ", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenOriginalFileNameIsMissing() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(file.getOriginalFilename()).thenReturn(null);

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsTooLarge() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getSize()).thenReturn(SupplierConstants.MAX_FILE_SIZE_BYTES + 1);

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenTypeIsInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", new byte[]{1}
        );
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        assertThrows(FileException.class, () -> service.upload(1L, "Video", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenReadingFails() throws IOException {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenThrow(new IOException("Erro na leitura"));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }
}
