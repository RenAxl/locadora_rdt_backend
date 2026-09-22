package com.locadora_rdt_backend.modules.customers.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileViewDTO;
import com.locadora_rdt_backend.modules.organization.customers.mapper.CustomerFileMapper;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.model.CustomerFile;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerFileRepository;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.modules.organization.customers.service.CustomerFileServiceImpl;
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
public class CustomerFileServiceTests {

    @Mock
    private CustomerFileRepository repository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerFileMapper mapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CustomerFileServiceImpl service;

    private Customer customer;
    private CustomerFile customerFile;
    private CustomerFileDTO customerFileDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Joao");

        customerFile = new CustomerFile();
        customerFile.setId(2L);
        customerFile.setCustomer(customer);
        customerFile.setName("Contrato");
        customerFile.setOriginalFileName("contrato.pdf");
        customerFile.setContentType("application/pdf");
        customerFile.setData(new byte[]{1, 2, 3});

        customerFileDTO = new CustomerFileDTO();
        customerFileDTO.setId(2L);
        customerFileDTO.setName("Contrato");
        customerFileDTO.setCustomerId(1L);
    }

    @Test
    void uploadShouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.save(any(CustomerFile.class))).thenReturn(customerFile);
        when(mapper.toDTO(customerFile)).thenReturn(customerFileDTO);

        CustomerFileDTO resultado = service.upload(1L, "Contrato", file);

        assertEquals(customerFileDTO, resultado);
        assertEquals(1L, resultado.getCustomerId());
        verify(repository).save(any(CustomerFile.class));
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[0]
        );
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void findAllByCustomerShouldReturnFiles() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findByCustomerIdOrderByIdDesc(1L))
                .thenReturn(Collections.singletonList(customerFile));
        when(mapper.toDTO(customerFile)).thenReturn(customerFileDTO);

        List<CustomerFileDTO> resultado = service.findAllByCustomer(1L);

        assertEquals(1, resultado.size());
        assertEquals("Contrato", resultado.get(0).getName());
    }

    @Test
    void findAllByCustomerShouldThrowExceptionWhenCustomerDoesNotExist() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findAllByCustomer(1L));

        verify(repository, never()).findByCustomerIdOrderByIdDesc(1L);
    }

    @Test
    void downloadShouldReturnFile() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findById(2L)).thenReturn(Optional.of(customerFile));

        CustomerFileViewDTO resultado = service.download(1L, 2L);

        assertNotNull(resultado);
        assertEquals("contrato.pdf", resultado.getFileName());
        assertEquals("application/pdf", resultado.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, resultado.getData());
    }

    @Test
    void downloadShouldThrowExceptionWhenFileDoesNotBelongToCustomer() {
        Customer outroCliente = new Customer();
        outroCliente.setId(3L);
        customerFile.setCustomer(outroCliente);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findById(2L)).thenReturn(Optional.of(customerFile));

        assertThrows(ResourceNotFoundException.class, () -> service.download(1L, 2L));
    }

    @Test
    void deleteShouldDeleteFile() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findById(2L)).thenReturn(Optional.of(customerFile));

        service.delete(1L, 2L);

        verify(repository).delete(customerFile);
    }

    @Test
    void deleteShouldThrowExceptionWhenFileDoesNotExist() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 2L));

        verify(repository, never()).delete(any(CustomerFile.class));
    }
    @Test
    void uploadShouldThrowExceptionWhenNameIsBlank() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(FileException.class, () -> service.upload(1L, " ", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenOriginalFileNameIsMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(file.getOriginalFilename()).thenReturn(null);

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsTooLarge() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getSize()).thenReturn(CustomerConstants.MAX_FILE_SIZE_BYTES + 1);

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenTypeIsInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", new byte[]{1}
        );
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(FileException.class, () -> service.upload(1L, "Video", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenReadingFails() throws IOException {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenThrow(new IOException("Erro na leitura"));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }
}
