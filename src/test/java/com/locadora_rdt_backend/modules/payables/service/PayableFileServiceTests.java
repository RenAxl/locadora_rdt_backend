package com.locadora_rdt_backend.modules.payables.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileViewDTO;
import com.locadora_rdt_backend.modules.financial.payables.mapper.PayableFileMapper;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.payables.model.PayableFile;
import com.locadora_rdt_backend.modules.financial.payables.repository.PayableFileRepository;
import com.locadora_rdt_backend.modules.financial.payables.repository.PayableRepository;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableFileServiceImpl;
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
public class PayableFileServiceTests {

    @Mock
    private PayableFileRepository repository;

    @Mock
    private PayableRepository payableRepository;

    @Mock
    private PayableFileMapper mapper;

    @InjectMocks
    private PayableFileServiceImpl service;

    private Payable payable;
    private PayableFile payableFile;
    private PayableFileDTO payableFileDTO;

    @BeforeEach
    void setUp() {
        payable = new Payable();
        payable.setId(1L);
        payable.setDescription("Aluguel");

        payableFile = new PayableFile();
        payableFile.setId(2L);
        payableFile.setPayable(payable);
        payableFile.setName("Contrato");
        payableFile.setOriginalFileName("contrato.pdf");
        payableFile.setContentType("application/pdf");
        payableFile.setData(new byte[]{1, 2, 3});

        payableFileDTO = new PayableFileDTO();
        payableFileDTO.setId(2L);
        payableFileDTO.setName("Contrato");
        payableFileDTO.setPayableId(1L);
    }

    @Test
    void uploadShouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.save(any(PayableFile.class))).thenReturn(payableFile);
        when(mapper.toDTO(payableFile)).thenReturn(payableFileDTO);

        PayableFileDTO resultado = service.upload(1L, "Contrato", file);

        assertEquals(payableFileDTO, resultado);
        assertEquals(1L, resultado.getPayableId());
        assertEquals("contrato.pdf", payable.getFileName());
        verify(repository).save(any(PayableFile.class));
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[0]
        );
        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void findAllByPayableShouldReturnFiles() {
        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.findByPayableIdOrderByIdDesc(1L))
                .thenReturn(Collections.singletonList(payableFile));
        when(mapper.toDTO(payableFile)).thenReturn(payableFileDTO);

        List<PayableFileDTO> resultado = service.findAllByPayable(1L);

        assertEquals(1, resultado.size());
        assertEquals("Contrato", resultado.get(0).getName());
    }

    @Test
    void findAllByPayableShouldThrowExceptionWhenPayableDoesNotExist() {
        when(payableRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findAllByPayable(1L));

        verify(repository, never()).findByPayableIdOrderByIdDesc(1L);
    }

    @Test
    void downloadShouldReturnFile() {
        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.findById(2L)).thenReturn(Optional.of(payableFile));

        PayableFileViewDTO resultado = service.download(1L, 2L);

        assertNotNull(resultado);
        assertEquals("contrato.pdf", resultado.getFileName());
        assertEquals("application/pdf", resultado.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, resultado.getData());
    }

    @Test
    void downloadShouldThrowExceptionWhenFileDoesNotBelongToPayable() {
        Payable outraConta = new Payable();
        outraConta.setId(3L);
        payableFile.setPayable(outraConta);

        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.findById(2L)).thenReturn(Optional.of(payableFile));

        assertThrows(ResourceNotFoundException.class, () -> service.download(1L, 2L));
    }

    @Test
    void deleteShouldDeleteFile() {
        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.findById(2L)).thenReturn(Optional.of(payableFile));

        service.delete(1L, 2L);

        verify(repository).delete(payableFile);
    }

    @Test
    void deleteShouldThrowExceptionWhenFileDoesNotExist() {
        when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 2L));

        verify(repository, never()).delete(any(PayableFile.class));
    }
}
