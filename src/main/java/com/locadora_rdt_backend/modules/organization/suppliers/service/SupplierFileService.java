package com.locadora_rdt_backend.modules.organization.suppliers.service;

import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileViewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupplierFileService {

    SupplierFileDTO upload(Long supplierId, String name, MultipartFile file);

    List<SupplierFileDTO> findAllBySupplier(Long supplierId);

    SupplierFileViewDTO download(Long supplierId, Long fileId);

    void delete(Long supplierId, Long fileId);
}
