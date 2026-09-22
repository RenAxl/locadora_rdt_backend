package com.locadora_rdt_backend.modules.customers.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDetailsDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerInsertDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerPhotoDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerUpdateDTO;
import com.locadora_rdt_backend.modules.organization.customers.mapper.CustomerMapper;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.modules.organization.customers.service.CustomerServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
public class CustomerServiceTests {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CustomerServiceImpl service;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Joao");
        customer.setEmail("joao@email.com");
        customer.setActive(true);

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Joao");
    }

    @Test
    void findAllPagedShouldReturnPageOfCustomers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Customer> customers = new PageImpl<>(Collections.singletonList(customer));

        when(repository.find("Joao", pageRequest)).thenReturn(customers);
        when(mapper.toDTO(customer)).thenReturn(customerDTO);

        Page<CustomerDTO> resultado = service.findAllPaged("Joao", pageRequest);

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
    void findByIdShouldReturnCustomer() {
        CustomerDetailsDTO detailsDTO = new CustomerDetailsDTO();
        detailsDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(mapper.toDetailsDTO(customer)).thenReturn(detailsDTO);

        CustomerDetailsDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenCustomerDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveCustomer() {
        CustomerInsertDTO insertDTO = new CustomerInsertDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(customer);
        when(repository.save(customer)).thenReturn(customer);
        when(mapper.toDTO(customer)).thenReturn(customerDTO);

        CustomerDTO resultado = service.insert(insertDTO);

        assertEquals(customerDTO, resultado);
        verify(repository).save(customer);
        assertEquals("Usuário Teste", customer.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        CustomerInsertDTO insertDTO = new CustomerInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(customer);
        when(repository.save(customer))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateCustomer() {
        CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.getOne(1L)).thenReturn(customer);
        when(repository.save(customer)).thenReturn(customer);
        when(mapper.toDTO(customer)).thenReturn(customerDTO);

        CustomerDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(customer, updateDTO);
        assertEquals(customerDTO, resultado);
        assertEquals("Usuário Teste", customer.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenCustomerDoesNotExist() {
        CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteCustomer() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void deleteAllShouldDeleteAllCustomers() {
        Customer segundoCliente = new Customer();
        segundoCliente.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(customer, segundoCliente));

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
    void getCustomerPhotoByIdShouldReturnPhoto() {
        byte[] foto = new byte[]{1, 2, 3};
        customer.setPhoto(foto);
        customer.setPhotoContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerPhotoDTO resultado = service.getCustomerPhotoById(1L);

        assertNotNull(resultado);
        assertArrayEquals(foto, resultado.getPhoto());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getCustomerPhotoByIdShouldThrowExceptionWhenCustomerDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCustomerPhotoById(1L));
    }
    @Test
    void updatePhotoShouldSavePhoto() {
        byte[] photo = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", photo);

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.updatePhoto(1L, file);

        assertArrayEquals(photo, customer.getPhoto());
        assertEquals("image/png", customer.getPhotoContentType());
        assertEquals("Usuário Teste", customer.getUpdatedBy());
        verify(repository).save(customer);
    }

    @Test
    void updatePhotoShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class, () -> service.updatePhoto(1L, file));

        verify(repository, never()).save(customer);
    }

    @Test
    void findEntityByIdShouldReturnCustomer() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        Customer resultado = service.findEntityById(1L);

        assertEquals(customer, resultado);
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenCustomerDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }

    @Test
    void getCustomerPhotoByIdShouldReturnNullWhenCustomerDoesNotHavePhoto() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerPhotoDTO resultado = service.getCustomerPhotoById(1L);

        assertNull(resultado);
    }

    @Test
    void deleteAllShouldThrowExceptionWhenOneIdDoesNotExist() {
        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Collections.singletonList(customer));

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteAll(Arrays.asList(1L, 2L)));

        verify(repository, never()).deleteAllByIds(any());
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenIdDoesNotExist() {
        when(repository.updateActiveById(1L, false)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class,
                () -> service.changeActiveStatus(1L, false));
    }
    @Test
    void updatePhotoShouldThrowExceptionWhenTypeIsInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1}
        );
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class, () -> service.updatePhoto(1L, file));

        verify(repository, never()).save(customer);
    }

    @Test
    void updatePhotoShouldThrowExceptionWhenPhotoIsTooLarge() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(CustomerConstants.MAX_PHOTO_SIZE_BYTES + 1);

        assertThrows(IllegalArgumentException.class, () -> service.updatePhoto(1L, file));

        verify(repository, never()).save(customer);
    }

    @Test
    void updatePhotoShouldThrowExceptionWhenReadingFails() throws IOException {
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenThrow(new IOException("Erro na leitura"));

        assertThrows(FileException.class, () -> service.updatePhoto(1L, file));

        verify(repository, never()).save(customer);
    }
}
