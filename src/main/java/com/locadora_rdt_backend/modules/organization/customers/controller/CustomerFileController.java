package com.locadora_rdt_backend.modules.organization.customers.controller;

import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileViewDTO;
import com.locadora_rdt_backend.modules.organization.customers.service.CustomerFileService;
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
@RequestMapping(value = "/customers/{customerId}/files")
public class CustomerFileController {

    private final CustomerFileService service;

    public CustomerFileController(CustomerFileService service) {
        this.service = service;
    }

    @PreAuthorize(CUSTOMER_WRITE)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerFileDTO> upload(
            @PathVariable Long customerId,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        CustomerFileDTO dto = service.upload(customerId, name, file);

        return ControllerResponseBuilder.created("/{fileId}", dto.getId(), dto);
    }

    @PreAuthorize(CUSTOMER_READ)
    @GetMapping
    public ResponseEntity<List<CustomerFileDTO>> findAllByCustomer(@PathVariable Long customerId) {
        List<CustomerFileDTO> list = service.findAllByCustomer(customerId);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(CUSTOMER_READ)
    @GetMapping(value = "/{fileId}/view")
    public ResponseEntity<byte[]> view(
            @PathVariable Long customerId,
            @PathVariable Long fileId) {

        CustomerFileViewDTO dto = service.download(customerId, fileId);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(CUSTOMER_READ)
    @GetMapping(value = "/{fileId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long customerId,
            @PathVariable Long fileId) {

        CustomerFileViewDTO dto = service.download(customerId, fileId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(CUSTOMER_DELETE)
    @DeleteMapping(value = "/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long customerId,
            @PathVariable Long fileId) {
        service.delete(customerId, fileId);
        return ResponseEntity.noContent().build();
    }
}
