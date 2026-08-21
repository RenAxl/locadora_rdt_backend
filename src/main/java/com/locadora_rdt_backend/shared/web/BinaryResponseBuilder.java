package com.locadora_rdt_backend.shared.web;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class BinaryResponseBuilder {

    private BinaryResponseBuilder() {
    }

    public static ResponseEntity<byte[]> noCacheMedia(byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.noCache())
                .body(data);
    }


}
