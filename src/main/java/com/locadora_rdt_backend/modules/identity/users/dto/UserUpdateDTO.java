package com.locadora_rdt_backend.modules.identity.users.dto;

import com.locadora_rdt_backend.modules.identity.users.validation.UserUpdateValid;
import com.locadora_rdt_backend.modules.identity.users.model.Address;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@UserUpdateValid
public class UserUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(min = 3, max = 60, message = "O nome deve ter entre 5 a 60 caracteres")
    @NotBlank(message = "Campo requerido")
    private String name;

    @NotBlank(message = "Campo requerido")
    @Email(message = "Favor informar um email válido")
    private String email;

    @NotNull(message = "Campo requerido")
    private Boolean active;

    @NotBlank(message = "Campo requerido")
    private String telephone;

    @Valid
    @NotNull(message = "Campo requerido")
    private Address address;

    @NotEmpty(message = "Informe pelo menos um perfil")
    private List<Long> roleIds = new ArrayList<>();

    public UserUpdateDTO() {
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getActive() {
        return active;
    }

    public String getTelephone() {
        return telephone;
    }

    public Address getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

}
