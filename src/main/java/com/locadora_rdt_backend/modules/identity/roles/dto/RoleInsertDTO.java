package com.locadora_rdt_backend.modules.identity.roles.dto;

import com.locadora_rdt_backend.modules.identity.roles.constants.RoleConstants;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class RoleInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = RoleConstants.AUTHORITY_REQUIRED)
    @Size(
            min = RoleConstants.AUTHORITY_MIN_LENGTH,
            max = RoleConstants.AUTHORITY_MAX_LENGTH,
            message = RoleConstants.AUTHORITY_LENGTH
    )
    private String authority;

    public RoleInsertDTO() {
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

}
