package com.locadora_rdt_backend.shared.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class ControllerResponseBuilder {

    private ControllerResponseBuilder() {
    }

    public static PageRequest pageRequest(Integer page, Integer linesPerPage, String direction, String orderBy) {
        return PageRequest.of(page, linesPerPage, Sort.Direction.valueOf(direction), orderBy);
    }

    public static <T> ResponseEntity<T> created(Long id, T body) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).body(body);
    }

    public static <T> ResponseEntity<T> created(String path, Long id, T body) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path(path)
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).body(body);
    }
}
