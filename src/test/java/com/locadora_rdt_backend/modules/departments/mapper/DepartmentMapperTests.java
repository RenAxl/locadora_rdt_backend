package com.locadora_rdt_backend.modules.departments.mapper;

import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentDTO;
import com.locadora_rdt_backend.modules.organization.departments.mapper.DepartmentMapper;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DepartmentMapperTests {
    @Test
    void toDTOShouldIncludeDescription() {
        Department department = new Department();
        department.setId(1L);
        department.setName("Financeiro");
        department.setDescription("Controle de contas");

        DepartmentDTO resultado = new DepartmentMapper().toDTO(department);

        assertEquals(1L, resultado.getId());
        assertEquals("Financeiro", resultado.getName());
        assertEquals("Controle de contas", resultado.getDescription());
    }

    @Test
    void toDTOShouldAcceptEmptyDescription() {
        Department department = new Department();
        department.setName("Financeiro");

        DepartmentDTO resultado = new DepartmentMapper().toDTO(department);

        assertNull(resultado.getDescription());
    }
    @Test
    void toDTOShouldIncludeAuditFields() {
        Department department = new Department();
        department.setCreatedAt(Instant.parse("2026-09-23T12:00:00Z"));
        department.setUpdatedAt(Instant.parse("2026-09-24T12:00:00Z"));
        department.setCreatedBy("Cadastro");
        department.setUpdatedBy("Edição");

        DepartmentDTO resultado = new DepartmentMapper().toDTO(department);

        assertEquals(department.getCreatedAt(), resultado.getCreatedAt());
        assertEquals(department.getUpdatedAt(), resultado.getUpdatedAt());
        assertEquals("Cadastro", resultado.getCreatedBy());
        assertEquals("Edição", resultado.getUpdatedBy());
    }

    @Test
    void toDTOShouldPreserveMissingAuditFields() {
        DepartmentDTO resultado = new DepartmentMapper().toDTO(new Department());

        assertNull(resultado.getCreatedAt());
        assertNull(resultado.getUpdatedAt());
        assertNull(resultado.getCreatedBy());
        assertNull(resultado.getUpdatedBy());
    }
}
