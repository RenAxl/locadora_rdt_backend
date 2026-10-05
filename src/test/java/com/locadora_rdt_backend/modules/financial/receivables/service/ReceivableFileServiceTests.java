package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileViewDTO;
import com.locadora_rdt_backend.modules.financial.receivables.mapper.ReceivableFileMapper;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.locadora_rdt_backend.modules.financial.receivables.model.ReceivableFile;
import com.locadora_rdt_backend.modules.financial.receivables.repository.ReceivableFileRepository;
import com.locadora_rdt_backend.modules.financial.receivables.repository.ReceivableRepository;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableFileServiceImpl;
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
public class ReceivableFileServiceTests {

    @Mock
    private ReceivableFileRepository repository;

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private ReceivableFileMapper mapper;

    @InjectMocks
    private ReceivableFileServiceImpl service;

    private Receivable receivable;
    private ReceivableFile receivableFile;
    private ReceivableFileDTO receivableFileDTO;

    @BeforeEach
    void setUp() {
        receivable = new Receivable();
        receivable.setId(1L);
        receivable.setDescription("Aluguel");

        receivableFile = new ReceivableFile();
        receivableFile.setId(2L);
        receivableFile.setReceivable(receivable);
        receivableFile.setName("Contrato");
        receivableFile.setOriginalFileName("contrato.pdf");
        receivableFile.setContentType("application/pdf");
        receivableFile.setData(new byte[]{1, 2, 3});

        receivableFileDTO = new ReceivableFileDTO();
        receivableFileDTO.setId(2L);
        receivableFileDTO.setName("Contrato");
        receivableFileDTO.setReceivableId(1L);
    }

    @Test
    void uploadShouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.save(any(ReceivableFile.class))).thenReturn(receivableFile);
        when(mapper.toDTO(receivableFile)).thenReturn(receivableFileDTO);

        ReceivableFileDTO resultado = service.upload(1L, "Contrato", file);

        assertEquals(receivableFileDTO, resultado);
        assertEquals(1L, resultado.getReceivableId());
        assertEquals("contrato.pdf", receivable.getFileName());
        verify(repository).save(any(ReceivableFile.class));
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[0]
        );
        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void findAllByReceivableShouldReturnFiles() {
        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.findByReceivableIdOrderByIdDesc(1L))
                .thenReturn(Collections.singletonList(receivableFile));
        when(mapper.toDTO(receivableFile)).thenReturn(receivableFileDTO);

        List<ReceivableFileDTO> resultado = service.findAllByReceivable(1L);

        assertEquals(1, resultado.size());
        assertEquals("Contrato", resultado.get(0).getName());
    }

    @Test
    void findAllByReceivableShouldThrowExceptionWhenReceivableDoesNotExist() {
        when(receivableRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findAllByReceivable(1L));

        verify(repository, never()).findByReceivableIdOrderByIdDesc(1L);
    }

    @Test
    void downloadShouldReturnFile() {
        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.findById(2L)).thenReturn(Optional.of(receivableFile));

        ReceivableFileViewDTO resultado = service.download(1L, 2L);

        assertNotNull(resultado);
        assertEquals("contrato.pdf", resultado.getFileName());
        assertEquals("application/pdf", resultado.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, resultado.getData());
    }

    @Test
    void downloadShouldThrowExceptionWhenFileDoesNotBelongToReceivable() {
        Receivable outraConta = new Receivable();
        outraConta.setId(3L);
        receivableFile.setReceivable(outraConta);

        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.findById(2L)).thenReturn(Optional.of(receivableFile));

        assertThrows(ResourceNotFoundException.class, () -> service.download(1L, 2L));
    }

    @Test
    void deleteShouldDeleteFile() {
        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.findById(2L)).thenReturn(Optional.of(receivableFile));

        service.delete(1L, 2L);

        verify(repository).delete(receivableFile);
    }

    @Test
    void deleteShouldThrowExceptionWhenFileDoesNotExist() {
        when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 2L));

        verify(repository, never()).delete(any(ReceivableFile.class));
    }
}
