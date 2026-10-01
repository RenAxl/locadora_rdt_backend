package com.locadora_rdt_backend.modules.financial.payables.controller;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileViewDTO;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableFileService;
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
@RequestMapping(value = "/payables/{payableId}/files")
public class PayableFileController {

    private final PayableFileService service;

    public PayableFileController(PayableFileService service) {
        this.service = service;
    }

    @PreAuthorize(PAYABLE_WRITE)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PayableFileDTO> upload(
            @PathVariable Long payableId,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        PayableFileDTO dto = service.upload(payableId, name, file);

        return ControllerResponseBuilder.created("/{fileId}", dto.getId(), dto);
    }

    @PreAuthorize(PAYABLE_READ)
    @GetMapping
    public ResponseEntity<List<PayableFileDTO>> findAllByPayable(@PathVariable Long payableId) {
        List<PayableFileDTO> list = service.findAllByPayable(payableId);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(PAYABLE_READ)
    @GetMapping(value = "/{fileId}/view")
    public ResponseEntity<byte[]> view(
            @PathVariable Long payableId,
            @PathVariable Long fileId) {

        PayableFileViewDTO dto = service.download(payableId, fileId);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(PAYABLE_READ)
    @GetMapping(value = "/{fileId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long payableId,
            @PathVariable Long fileId) {

        PayableFileViewDTO dto = service.download(payableId, fileId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(PAYABLE_DELETE)
    @DeleteMapping(value = "/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long payableId,
            @PathVariable Long fileId) {
        service.delete(payableId, fileId);
        return ResponseEntity.noContent().build();
    }
}
