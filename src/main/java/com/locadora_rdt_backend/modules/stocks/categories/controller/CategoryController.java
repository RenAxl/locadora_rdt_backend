package com.locadora_rdt_backend.modules.stocks.categories.controller;

import com.locadora_rdt_backend.modules.stocks.categories.dto.*;
import com.locadora_rdt_backend.modules.stocks.categories.service.CategoryService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/rental/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PreAuthorize(CATEGORY_READ)
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<CategoryDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(CATEGORY_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable Long id) {
        CategoryDTO categoryDto = service.findById(id);
        return ResponseEntity.ok().body(categoryDto);
    }

    @PreAuthorize(CATEGORY_WRITE)
    @PostMapping
    public ResponseEntity<CategoryDTO> insert(@Valid @RequestBody CategoryInsertDTO dto) {
        CategoryDTO categoryDto = service.insert(dto);
        return ControllerResponseBuilder.created(categoryDto.getId(), categoryDto);
    }

    @PreAuthorize(CATEGORY_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateDTO dto) {
        CategoryDTO categoryDto = service.update(id, dto);
        return ResponseEntity.ok().body(categoryDto);
    }

    @PreAuthorize(CATEGORY_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(CATEGORY_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(CATEGORY_WRITE)
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeActive(@PathVariable Long id, @RequestBody boolean active) {
        service.changeActiveStatus(id, active);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(CATEGORY_READ)
    @GetMapping(value = "/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {

        CategoryImageDTO dto = service.getCategoryImageById(id);

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getContentType()))
                .body(dto.getImage());
    }

    @PreAuthorize(CATEGORY_WRITE)
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        service.updateImage(id, file);
        return ResponseEntity.noContent().build();
    }

}
