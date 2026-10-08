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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RentalHistoryServiceImpl implements RentalHistoryService {

    private final RentalHistoryRepository repository;
    private final RentalItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final RentalHistoryMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public RentalHistoryServiceImpl(
            RentalHistoryRepository repository,
            RentalItemRepository itemRepository,
            CustomerRepository customerRepository,
            RentalHistoryMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalHistoryDTO> findAllPaged(PageRequest pageRequest) {

        String email = authenticationFacade.getAuthenticatedUsername();
        Customer customer = customerRepository.findByEmail(email);

        if (customer == null) {
            throw new ResourceNotFoundException(RentalHistoryConstants.CUSTOMER_NOT_FOUND);
        }

        Page<Rental> rentals = repository.find(email, pageRequest);

        Page<RentalHistoryDTO> rentalsDTO = rentals.map(rental -> {
            List<RentalItem> items = itemRepository.findByRentalIdOrderById(rental.getId());
            rental.setItems(items);

            RentalHistoryDTO dto = mapper.toDTO(rental);

            return dto;
        });

        return rentalsDTO;
    }
}
