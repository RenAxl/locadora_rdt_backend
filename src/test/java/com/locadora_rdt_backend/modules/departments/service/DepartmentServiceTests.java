package com.locadora_rdt_backend.modules.departments.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.departments.constants.DepartmentConstants;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentDTO;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentDetailsDTO;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentInsertDTO;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentUpdateDTO;
import com.locadora_rdt_backend.modules.organization.departments.mapper.DepartmentMapper;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import com.locadora_rdt_backend.modules.organization.departments.repository.DepartmentRepository;
import com.locadora_rdt_backend.modules.organization.departments.service.DepartmentServiceImpl;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
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
public class DepartmentServiceTests {

    @Mock
    private DepartmentRepository repository;

    @Mock
    private DepartmentMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private DepartmentServiceImpl service;

    private Department department;
    private DepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Financeiro");
        department.setDescription("Contas da locadora");

        departmentDTO = new DepartmentDTO();
        departmentDTO.setId(1L);
        departmentDTO.setName("Financeiro");
    }

    @Test
    void findAllPagedShouldReturnPageOfDepartments() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Department> departments = new PageImpl<>(Collections.singletonList(department));

        when(repository.find("Financeiro", pageRequest)).thenReturn(departments);
        when(mapper.toDTO(department)).thenReturn(departmentDTO);

        Page<DepartmentDTO> resultado = service.findAllPaged("Financeiro", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Financeiro", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Financeiro", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Financeiro", pageRequest));
    }

    @Test
    void findAllPagedShouldTrimName() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Department> departments = new PageImpl<>(Collections.singletonList(department));

        when(repository.find("Financeiro", pageRequest)).thenReturn(departments);
        when(mapper.toDTO(department)).thenReturn(departmentDTO);

        Page<DepartmentDTO> resultado = service.findAllPaged(" Financeiro ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        verify(repository).find("Financeiro", pageRequest);
    }

    @Test
    void findAllPagedShouldUseEmptyNameWhenNameIsNull() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Department> departments = new PageImpl<>(Collections.emptyList());

        when(repository.find("", pageRequest)).thenReturn(departments);

        Page<DepartmentDTO> resultado = service.findAllPaged(null, pageRequest);

        assertEquals(0, resultado.getTotalElements());
        verify(repository).find("", pageRequest);
    }

    @Test
    void findByIdShouldReturnDepartment() {
        DepartmentDetailsDTO detailsDTO = new DepartmentDetailsDTO();
        detailsDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(department));
        when(mapper.toDetailsDTO(department)).thenReturn(detailsDTO);

        DepartmentDetailsDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenDepartmentDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(DepartmentConstants.DEPARTMENT_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDetailsDTO(any());
    }

    @Test
    void insertShouldSaveDepartment() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        DepartmentInsertDTO insertDTO = new DepartmentInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(department);
        when(repository.save(department)).thenReturn(department);
        when(mapper.toDTO(department)).thenReturn(departmentDTO);

        DepartmentDTO resultado = service.insert(insertDTO);

        assertEquals(departmentDTO, resultado);
        assertEquals("Usuário Teste", department.getCreatedBy());
        verify(repository).save(department);
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        DepartmentInsertDTO insertDTO = new DepartmentInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(department);
        when(repository.save(department))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateDepartment() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        DepartmentUpdateDTO updateDTO = new DepartmentUpdateDTO();

        when(repository.getOne(1L)).thenReturn(department);
        when(repository.save(department)).thenReturn(department);
        when(mapper.toDTO(department)).thenReturn(departmentDTO);

        DepartmentDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(department, updateDTO);
        assertEquals(departmentDTO, resultado);
        assertEquals("Usuário Teste", department.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenDepartmentDoesNotExist() {
        DepartmentUpdateDTO updateDTO = new DepartmentUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L, updateDTO));

        assertEquals(DepartmentConstants.DEPARTMENT_NOT_FOUND, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteShouldDeleteDepartment() {
        when(repository.findById(1L)).thenReturn(Optional.of(department));

        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.delete(1L));

        assertEquals(DepartmentConstants.DEPARTMENT_NOT_FOUND, exception.getMessage());
        verify(repository, never()).deleteById(any());
        verify(repository, never()).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenDatabaseIntegrityIsViolated() {
        when(repository.findById(1L)).thenReturn(Optional.of(department));
        doThrow(new DataIntegrityViolationException("Setor vinculado"))
                .when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.delete(1L));

        assertEquals(DepartmentConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteShouldThrowExceptionWhenRepositoryDoesNotFindId() {
        when(repository.findById(1L)).thenReturn(Optional.of(department));
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));

        verify(repository, never()).flush();
    }

    @Test
    void findEntityByIdShouldReturnDepartment() {
        when(repository.findById(1L)).thenReturn(Optional.of(department));

        Department resultado = service.findEntityById(1L);

        assertNotNull(resultado);
        assertEquals(department, resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenDepartmentDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findEntityById(1L));

        assertEquals(DepartmentConstants.DEPARTMENT_NOT_FOUND, exception.getMessage());
    }
}
