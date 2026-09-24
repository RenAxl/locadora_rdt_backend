package com.locadora_rdt_backend.modules.organization.customers.service;

import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerPhotoDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerInsertDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerUpdateDTO;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {

    Page<CustomerDTO> findAllPaged(String name, PageRequest pageRequest);

    CustomerDTO findById(Long id);

    CustomerDTO insert(CustomerInsertDTO dto);

    CustomerDTO update(Long id, CustomerUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    CustomerPhotoDTO getCustomerPhotoById(Long id);

    void updatePhoto(Long id, MultipartFile file);

    Customer findEntityById(Long id);

}
