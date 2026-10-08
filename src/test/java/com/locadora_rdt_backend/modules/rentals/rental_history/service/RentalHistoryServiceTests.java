package com.locadora_rdt_backend.modules.rentals.rental_history.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalItemRepository;
import com.locadora_rdt_backend.modules.rentals.rental_history.constants.RentalHistoryConstants;
import com.locadora_rdt_backend.modules.rentals.rental_history.dto.RentalHistoryDTO;
import com.locadora_rdt_backend.modules.rentals.rental_history.mapper.RentalHistoryMapper;
import com.locadora_rdt_backend.modules.rentals.rental_history.repository.RentalHistoryRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalHistoryServiceTests {

    @Mock
    private RentalHistoryRepository repository;

    @Mock
    private RentalItemRepository itemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RentalHistoryMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private RentalHistoryServiceImpl service;

    private Customer customer;
    private Rental rental;
    private RentalItem rentalItem;
    private RentalHistoryDTO rentalHistoryDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Joao");
        customer.setEmail("joao@email.com");

        rental = new Rental();
        rental.setId(2L);
        rental.setCustomer(customer);
        rental.setRentalNumber("LOC-2");

        rentalItem = new RentalItem();
        rentalItem.setId(3L);
        rentalItem.setRental(rental);

        rentalHistoryDTO = new RentalHistoryDTO();
        rentalHistoryDTO.setId(2L);
        rentalHistoryDTO.setRentalNumber("LOC-2");
    }

    @Test
    void findAllPagedShouldReturnPageOfCurrentCustomerRentals() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "rental_date");
        Page<Rental> rentals = new PageImpl<>(Collections.singletonList(rental), pageRequest, 1);

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(customer);
        when(repository.find("joao@email.com", pageRequest)).thenReturn(rentals);
        when(itemRepository.findByRentalIdOrderById(2L)).thenReturn(Collections.singletonList(rentalItem));
        when(mapper.toDTO(rental)).thenReturn(rentalHistoryDTO);

        Page<RentalHistoryDTO> resultado = service.findAllPaged(pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("LOC-2", resultado.getContent().get(0).getRentalNumber());
        assertEquals(pageRequest, resultado.getPageable());
        assertEquals(Collections.singletonList(rentalItem), rental.getItems());
        verify(repository).find("joao@email.com", pageRequest);
        verify(itemRepository).findByRentalIdOrderById(2L);
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenCurrentCustomerDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findAllPaged(pageRequest));

        assertEquals(RentalHistoryConstants.CUSTOMER_NOT_FOUND, exception.getMessage());
        verify(repository, never()).find(any(), any());
        verify(itemRepository, never()).findByRentalIdOrderById(any());
    }
}
