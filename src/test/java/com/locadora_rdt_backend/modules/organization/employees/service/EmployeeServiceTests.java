package com.locadora_rdt_backend.modules.organization.employees.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeDTO;
import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import com.locadora_rdt_backend.modules.organization.positions.service.PositionService;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import com.locadora_rdt_backend.modules.organization.departments.service.DepartmentService;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeInsertDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeePhotoDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeUpdateDTO;
import com.locadora_rdt_backend.modules.organization.employees.mapper.EmployeeMapper;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;
import com.locadora_rdt_backend.modules.organization.employees.service.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
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
public class EmployeeServiceTests {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private PositionService positionService;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private EmployeeServiceImpl service;

    private Employee employee;
    private EmployeeDTO employeeDTO;
    private Position position;
    private Department department;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Joao");
        employee.setEmail("joao@email.com");
        employee.setActive(true);

        position = new Position();
        position.setId(2L);
        position.setName("Motorista");

        department = new Department();
        department.setId(3L);
        department.setName("Transportes");

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setName("Joao");
    }

    @Test
    void findAllPagedShouldReturnPageOfEmployees() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Employee> employees = new PageImpl<>(Collections.singletonList(employee));

        when(repository.find("Joao", pageRequest)).thenReturn(employees);
        when(mapper.toDTO(employee)).thenReturn(employeeDTO);

        Page<EmployeeDTO> resultado = service.findAllPaged("Joao", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Joao", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Joao", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Joao", pageRequest));
    }

    @Test
    void findByIdShouldReturnEmployee() {
        EmployeeDTO detailsDTO = new EmployeeDTO();
        detailsDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(mapper.toDTO(employee)).thenReturn(detailsDTO);

        EmployeeDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenEmployeeDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveEmployee() {
        EmployeeInsertDTO insertDTO = new EmployeeInsertDTO();
        insertDTO.setPositionId(2L);
        insertDTO.setDepartmentId(3L);
        when(positionService.findEntityById(2L)).thenReturn(position);
        when(departmentService.findEntityById(3L)).thenReturn(department);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(employee);
        when(repository.save(employee)).thenReturn(employee);
        when(mapper.toDTO(employee)).thenReturn(employeeDTO);

        EmployeeDTO resultado = service.insert(insertDTO);

        assertEquals(employeeDTO, resultado);
        assertEquals(position, employee.getPosition());
        assertEquals(department, employee.getDepartment());
        verify(positionService).findEntityById(2L);
        verify(departmentService).findEntityById(3L);
        verify(repository).save(employee);
        assertEquals("Usuário Teste", employee.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        EmployeeInsertDTO insertDTO = new EmployeeInsertDTO();
        insertDTO.setPositionId(2L);
        insertDTO.setDepartmentId(3L);
        when(positionService.findEntityById(2L)).thenReturn(position);
        when(departmentService.findEntityById(3L)).thenReturn(department);

        when(mapper.toEntity(insertDTO)).thenReturn(employee);
        when(repository.save(employee))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateEmployee() {
        EmployeeUpdateDTO updateDTO = new EmployeeUpdateDTO();
        updateDTO.setPositionId(2L);
        updateDTO.setDepartmentId(3L);
        when(positionService.findEntityById(2L)).thenReturn(position);
        when(departmentService.findEntityById(3L)).thenReturn(department);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.getOne(1L)).thenReturn(employee);
        when(repository.save(employee)).thenReturn(employee);
        when(mapper.toDTO(employee)).thenReturn(employeeDTO);

        EmployeeDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(employee, updateDTO);
        assertEquals(employeeDTO, resultado);
        assertEquals(position, employee.getPosition());
        assertEquals(department, employee.getDepartment());
        verify(positionService).findEntityById(2L);
        verify(departmentService).findEntityById(3L);
        assertEquals("Usuário Teste", employee.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenEmployeeDoesNotExist() {
        EmployeeUpdateDTO updateDTO = new EmployeeUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteEmployee() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void deleteAllShouldDeleteAllEmployees() {
        Employee segundoFuncionario = new Employee();
        segundoFuncionario.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(employee, segundoFuncionario));

        service.deleteAll(Arrays.asList(1L, 2L));

        verify(repository).deleteAllByIds(Arrays.asList(1L, 2L));
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAll(Collections.emptyList()));

        verify(repository, never()).deleteAllByIds(any());
    }

    @Test
    void changeActiveStatusShouldChangeStatus() {
        when(repository.updateActiveById(1L, false)).thenReturn(1);

        service.changeActiveStatus(1L, false);

        verify(repository).updateActiveById(1L, false);
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenDatabaseFails() {
        when(repository.updateActiveById(1L, false))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DatabaseException.class,
                () -> service.changeActiveStatus(1L, false));
    }

    @Test
    void getEmployeePhotoByIdShouldReturnPhoto() {
        byte[] foto = new byte[]{1, 2, 3};
        employee.setPhoto(foto);
        employee.setPhotoContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeePhotoDTO resultado = service.getEmployeePhotoById(1L);

        assertNotNull(resultado);
        assertArrayEquals(foto, resultado.getPhoto());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getEmployeePhotoByIdShouldThrowExceptionWhenEmployeeDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getEmployeePhotoById(1L));
    }
    @Test
    void updatePhotoShouldSavePhoto() {
        byte[] photo = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", photo);

        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.updatePhoto(1L, file);

        assertArrayEquals(photo, employee.getPhoto());
        assertEquals("image/png", employee.getPhotoContentType());
        assertEquals("Usuário Teste", employee.getUpdatedBy());
        verify(repository).save(employee);
    }

    @Test
    void updatePhotoShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> service.updatePhoto(1L, file));

        verify(repository, never()).save(employee);
    }

    @Test
    void findEntityByIdShouldReturnEmployee() {
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        Employee resultado = service.findEntityById(1L);

        assertEquals(employee, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenEmployeeDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }
}
