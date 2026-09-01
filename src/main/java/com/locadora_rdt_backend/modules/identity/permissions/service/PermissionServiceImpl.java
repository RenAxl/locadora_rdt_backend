package com.locadora_rdt_backend.modules.identity.permissions.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.identity.permissions.constants.PermissionConstants;
import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;
import com.locadora_rdt_backend.modules.identity.permissions.model.Permission;
import com.locadora_rdt_backend.modules.identity.permissions.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository repository;

    public PermissionServiceImpl(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> findAll(String groupName) {
        List<Permission> list;
        if (groupName == null || groupName.trim().isEmpty()) {
            list = repository.findAllByOrderByGroupNameAscNameAsc();
        } else {
            list = repository.findByGroupNameIgnoreCaseOrderByNameAsc(groupName.trim());
        }
        return list.stream().map(PermissionDTO::new).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllGroupNames() {
        return repository.findDistinctGroupNames();
    }

    @Override
    @Transactional(readOnly = true)
    public Permission findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PermissionConstants.PERMISSION_NOT_FOUND
                ));
    }
}
