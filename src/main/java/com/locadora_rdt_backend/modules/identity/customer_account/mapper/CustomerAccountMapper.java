package com.locadora_rdt_backend.modules.identity.customer_account.mapper;

import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountRegistrationDTO;
import com.locadora_rdt_backend.modules.identity.users.model.Address;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerAccountMapper {

    public CustomerAccountMapper() {
    }

    public Customer toCustomer(CustomerAccountRegistrationDTO dto) {

        Customer customer = new Customer();

        customer.setName(dto.getName().trim());
        customer.setCpf(onlyNumbers(dto.getCpf()));
        customer.setEmail(dto.getEmail().trim().toLowerCase());
        customer.setPhone(onlyNumbers(dto.getPhone()));
        customer.setAddress(toCustomerAddress(dto));
        customer.setActive(true);

        return customer;
    }

    public User toUser(CustomerAccountRegistrationDTO dto) {

        User user = new User();

        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setTelephone(onlyNumbers(dto.getPhone()));
        user.setAddress(toUserAddress(dto));

        return user;
    }

    private com.locadora_rdt_backend.modules.organization.customers.model.Address toCustomerAddress(
            CustomerAccountRegistrationDTO dto
    ) {

        com.locadora_rdt_backend.modules.organization.customers.model.Address address =
                new com.locadora_rdt_backend.modules.organization.customers.model.Address();

        address.setStreet(dto.getStreet().trim());
        address.setNumber(dto.getNumber().trim());
        address.setComplement(trim(dto.getComplement()));
        address.setNeighborhood(dto.getNeighborhood().trim());
        address.setCity(dto.getCity().trim());
        address.setState(dto.getState().trim().toUpperCase());
        address.setZipCode(dto.getZipCode().trim());

        return address;
    }

    private Address toUserAddress(CustomerAccountRegistrationDTO dto) {

        Address address = new Address();

        address.setStreet(dto.getStreet().trim());
        address.setNumber(dto.getNumber().trim());
        address.setComplement(trim(dto.getComplement()));
        address.setNeighborhood(dto.getNeighborhood().trim());
        address.setCity(dto.getCity().trim());
        address.setState(dto.getState().trim().toUpperCase());
        address.setZipCode(dto.getZipCode().trim());

        return address;
    }

    private String onlyNumbers(String value) {

        if (value == null) {
            return null;
        }

        return value.replaceAll(CustomerAccountConstants.NON_DIGIT_PATTERN, "");
    }

    private String trim(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
