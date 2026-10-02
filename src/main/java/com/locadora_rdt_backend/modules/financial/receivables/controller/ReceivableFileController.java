package com.locadora_rdt_backend.modules.financial.receivables.controller;

import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileViewDTO;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableFileService;
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
@RequestMapping(value = "/receivables/{receivableId}/files")
public class ReceivableFileController {

    private final ReceivableFileService service;

    public ReceivableFileController(ReceivableFileService service) {
        this.service = service;
    }

    @PreAuthorize(RECEIVABLE_WRITE)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceivableFileDTO> upload(
            @PathVariable Long receivableId,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        ReceivableFileDTO dto = service.upload(receivableId, name, file);

        return ControllerResponseBuilder.created("/{fileId}", dto.getId(), dto);
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping
    public ResponseEntity<List<ReceivableFileDTO>> findAllByReceivable(@PathVariable Long receivableId) {
        List<ReceivableFileDTO> list = service.findAllByReceivable(receivableId);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping(value = "/{fileId}/view")
    public ResponseEntity<byte[]> view(
            @PathVariable Long receivableId,
            @PathVariable Long fileId) {

        ReceivableFileViewDTO dto = service.download(receivableId, fileId);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping(value = "/{fileId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long receivableId,
            @PathVariable Long fileId) {

        ReceivableFileViewDTO dto = service.download(receivableId, fileId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(RECEIVABLE_DELETE)
    @DeleteMapping(value = "/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long receivableId,
            @PathVariable Long fileId) {
        service.delete(receivableId, fileId);
        return ResponseEntity.noContent().build();
    }
}
