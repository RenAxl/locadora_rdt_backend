package com.locadora_rdt_backend.modules.organization.employees.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.organization.employees.constants.EmployeeConstants;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileViewDTO;
import com.locadora_rdt_backend.modules.organization.employees.mapper.EmployeeFileMapper;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.employees.model.EmployeeFile;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeFileRepository;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;
import com.locadora_rdt_backend.modules.organization.employees.service.EmployeeFileServiceImpl;
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
public class EmployeeFileServiceTests {

    @Mock
    private EmployeeFileRepository repository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeFileMapper mapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private EmployeeFileServiceImpl service;

    private Employee employee;
    private EmployeeFile employeeFile;
    private EmployeeFileDTO employeeFileDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Joao");

        employeeFile = new EmployeeFile();
        employeeFile.setId(2L);
        employeeFile.setEmployee(employee);
        employeeFile.setName("Contrato");
        employeeFile.setOriginalFileName("contrato.pdf");
        employeeFile.setContentType("application/pdf");
        employeeFile.setData(new byte[]{1, 2, 3});

        employeeFileDTO = new EmployeeFileDTO();
        employeeFileDTO.setId(2L);
        employeeFileDTO.setName("Contrato");
        employeeFileDTO.setEmployeeId(1L);
    }

    @Test
    void uploadShouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(any(EmployeeFile.class))).thenReturn(employeeFile);
        when(mapper.toDTO(employeeFile)).thenReturn(employeeFileDTO);

        EmployeeFileDTO resultado = service.upload(1L, "Contrato", file);

        assertEquals(employeeFileDTO, resultado);
        assertEquals(1L, resultado.getEmployeeId());
        verify(repository).save(any(EmployeeFile.class));
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[0]
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void findAllByEmployeeShouldReturnFiles() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.findByEmployeeIdOrderByIdDesc(1L))
                .thenReturn(Collections.singletonList(employeeFile));
        when(mapper.toDTO(employeeFile)).thenReturn(employeeFileDTO);

        List<EmployeeFileDTO> resultado = service.findAllByEmployee(1L);

        assertEquals(1, resultado.size());
        assertEquals("Contrato", resultado.get(0).getName());
    }

    @Test
    void findAllByEmployeeShouldThrowExceptionWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findAllByEmployee(1L));

        verify(repository, never()).findByEmployeeIdOrderByIdDesc(1L);
    }

    @Test
    void downloadShouldReturnFile() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.findById(2L)).thenReturn(Optional.of(employeeFile));

        EmployeeFileViewDTO resultado = service.download(1L, 2L);

        assertNotNull(resultado);
        assertEquals("contrato.pdf", resultado.getFileName());
        assertEquals("application/pdf", resultado.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, resultado.getData());
    }

    @Test
    void downloadShouldThrowExceptionWhenFileDoesNotBelongToEmployee() {
        Employee outroFuncionario = new Employee();
        outroFuncionario.setId(3L);
        employeeFile.setEmployee(outroFuncionario);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.findById(2L)).thenReturn(Optional.of(employeeFile));

        assertThrows(ResourceNotFoundException.class, () -> service.download(1L, 2L));
    }

    @Test
    void deleteShouldDeleteFile() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.findById(2L)).thenReturn(Optional.of(employeeFile));

        service.delete(1L, 2L);

        verify(repository).delete(employeeFile);
    }

    @Test
    void deleteShouldThrowExceptionWhenFileDoesNotExist() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 2L));

        verify(repository, never()).delete(any(EmployeeFile.class));
    }
    @Test
    void uploadShouldThrowExceptionWhenNameIsBlank() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(FileException.class, () -> service.upload(1L, " ", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenOriginalFileNameIsMissing() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(file.getOriginalFilename()).thenReturn(null);

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenFileIsTooLarge() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getSize()).thenReturn(EmployeeConstants.MAX_FILE_SIZE_BYTES + 1);

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenTypeIsInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", new byte[]{1}
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(FileException.class, () -> service.upload(1L, "Video", file));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadShouldThrowExceptionWhenReadingFails() throws IOException {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenThrow(new IOException("Erro na leitura"));

        assertThrows(FileException.class, () -> service.upload(1L, "Contrato", file));

        verify(repository, never()).save(any());
    }
}
