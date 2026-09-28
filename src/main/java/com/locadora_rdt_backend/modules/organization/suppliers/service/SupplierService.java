package com.locadora_rdt_backend.modules.organization.suppliers.service;

import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierImageDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierInsertDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierUpdateDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

public interface SupplierService {

    Page<SupplierDTO> findAllPaged(String name, PageRequest pageRequest);

    SupplierDTO findById(Long id);

    SupplierDTO insert(SupplierInsertDTO dto);

    SupplierDTO update(Long id, SupplierUpdateDTO dto);

    void delete(Long id);

    SupplierImageDTO getSupplierImageById(Long id);

    void updateImage(Long id, MultipartFile file);

    Supplier findEntityById(Long id);

}
