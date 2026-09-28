package com.locadora_rdt_backend.modules.organization.suppliers.controller;

import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileViewDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.service.SupplierFileService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/suppliers/{supplierId}/files")
public class SupplierFileController {

    private final SupplierFileService service;

    public SupplierFileController(SupplierFileService service) {
        this.service = service;
    }

    @PreAuthorize(SUPPLIER_WRITE)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SupplierFileDTO> upload(
            @PathVariable Long supplierId,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        SupplierFileDTO dto = service.upload(supplierId, name, file);

        return ControllerResponseBuilder.created("/{fileId}", dto.getId(), dto);
    }

    @PreAuthorize(SUPPLIER_READ)
    @GetMapping
    public ResponseEntity<List<SupplierFileDTO>> findAllBySupplier(@PathVariable Long supplierId) {
        List<SupplierFileDTO> list = service.findAllBySupplier(supplierId);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(SUPPLIER_READ)
    @GetMapping(value = "/{fileId}/view")
    public ResponseEntity<byte[]> view(
            @PathVariable Long supplierId,
            @PathVariable Long fileId) {

        SupplierFileViewDTO dto = service.download(supplierId, fileId);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(SUPPLIER_READ)
    @GetMapping(value = "/{fileId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long supplierId,
            @PathVariable Long fileId) {

        SupplierFileViewDTO dto = service.download(supplierId, fileId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(SUPPLIER_DELETE)
    @DeleteMapping(value = "/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long supplierId,
            @PathVariable Long fileId) {
        service.delete(supplierId, fileId);
        return ResponseEntity.noContent().build();
    }
}
