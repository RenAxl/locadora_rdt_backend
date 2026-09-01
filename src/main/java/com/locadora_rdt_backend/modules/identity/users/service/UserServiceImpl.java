package com.locadora_rdt_backend.modules.identity.users.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.identity.users.dto.*;
import com.locadora_rdt_backend.modules.identity.users.mapper.UserMapper;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import com.locadora_rdt_backend.modules.identity.roles.service.RoleService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final RoleService roleService;

    public UserServiceImpl(
            UserRepository repository,
            UserMapper mapper,
            RoleService roleService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(String name, PageRequest pageRequest) {

        Page<User> users = repository.find(name, pageRequest);

        Page<UserDTO> usersDTO = users.map(user -> mapper.toDTO(user));

        return usersDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsDTO findById(Long id) {

        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("Usuário Não encontrado");
        }

        User user = userOptional.get();

        UserDetailsDTO userDTO = mapper.toDetailsDTO(user);

        return userDTO;
    }

    @Override
    @Transactional
    public UserDTO insert(UserInsertDTO dto) {

        User user = mapper.toEntity(dto);

        user.setPassword(null);
        user.setActive(false);

        user.setCreatedBy("Usuário Teste");

        for (Long roleId : dto.getRoleIds()) {
            Role role = roleService.findEntityById(roleId);
            user.getRoles().add(role);
        }

        User savedUser = repository.save(user);

        UserDTO userDTO = mapper.toDTO(savedUser);

        return userDTO;
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {

        try {

            User user = repository.getOne(id);

            mapper.updateEntity(user, dto);

            user.getRoles().clear();

            for (Long roleId : dto.getRoleIds()) {
                Role role = roleService.findEntityById(roleId);
                user.getRoles().add(role);
            }

            user.setUpdatedBy("Usuário Teste");

            User savedUser = repository.save(user);

            UserDTO userDTO = mapper.toDTO(savedUser);

            return userDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException("Usuário Não encontrado");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id não encontrado");
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Lista de ids vazia");
        }

        List<User> users = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (User user : users) {
            existingIds.add(user.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException("Um ou mais IDs não existem");
        }

        repository.deleteAllByIds(ids);
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException("Id not found ");
            }

        } catch (DataAccessException e) {

            throw new DatabaseException("Erro ao alterar o status do usuário.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserPhotoDTO getUserPhotoById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("O Id é nulo");
        }

        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        User user = userOptional.get();

        byte[] photo = user.getPhoto();

        if (photo == null || photo.length == 0) {
            return null;
        }

        String photoContentType = user.getPhotoContentType();

        UserPhotoDTO userPhotoDTO = new UserPhotoDTO(
                photo,
                photoContentType
        );

        return userPhotoDTO;
    }

}
