package com.locadora_rdt_backend.modules.identity.users.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.identity.account_activation.service.AccountActivationService;
import com.locadora_rdt_backend.modules.identity.users.constants.UserConstants;
import com.locadora_rdt_backend.modules.identity.users.dto.*;
import com.locadora_rdt_backend.modules.identity.users.mapper.UserMapper;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import com.locadora_rdt_backend.modules.identity.roles.service.RoleService;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import com.locadora_rdt_backend.modules.identity.account_activation.repository.AccountActivationRepository;
import com.locadora_rdt_backend.modules.identity.password_recovery.repository.PasswordRecoveryRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final AccountActivationRepository activationRepository;
    private final PasswordRecoveryRepository recoveryRepository;
    private final UserRepository repository;
    private final UserMapper mapper;
    private final RoleService roleService;
    private final AccountActivationService accountActivationService;
    private final AuthenticationFacade authenticationFacade;

    public UserServiceImpl(
            UserRepository repository,
            UserMapper mapper,
            RoleService roleService,
            AccountActivationService accountActivationService,
            AuthenticationFacade authenticationFacade,
            AccountActivationRepository activationRepository,
            PasswordRecoveryRepository recoveryRepository
    ) {
        this.activationRepository = activationRepository;
        this.recoveryRepository = recoveryRepository;
        this.repository = repository;
        this.mapper = mapper;
        this.roleService = roleService;
        this.accountActivationService = accountActivationService;
        this.authenticationFacade = authenticationFacade;
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
    public UserDTO findById(Long id) {

        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException(UserConstants.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        UserDTO userDTO = mapper.toDTO(user);

        return userDTO;
    }

    @Override
    @Transactional
    public UserDTO insert(UserInsertDTO dto) {

        User user = mapper.toEntity(dto);

        user.setPassword(null);
        user.setActive(false);

        user.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        for (Long roleId : dto.getRoleIds()) {
            Role role = roleService.findEntityById(roleId);
            user.getRoles().add(role);
        }

        User savedUser = repository.save(user);

        UserDTO userDTO = mapper.toDTO(savedUser);

        accountActivationService
                .createActivationTokenAndSendEmail(savedUser);

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

            user.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            User savedUser = repository.save(user);

            UserDTO userDTO = mapper.toDTO(savedUser);

            return userDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(UserConstants.USER_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(UserConstants.ID_NOT_FOUND));
        validateDeletion(user);
        deleteTokens(user);
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(UserConstants.ID_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(UserConstants.EMPTY_ID_LIST);
        }

        List<User> users = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (User user : users) {
            existingIds.add(user.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(UserConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        users.forEach(this::validateDeletion);
        users.forEach(this::deleteTokens);
        repository.deleteAll(users);
    }

    private void deleteTokens(User user) {
        activationRepository.deleteByUserId(user.getId());
        recoveryRepository.deleteByUserId(user.getId());
    }

    private void validateDeletion(User user) {
        if (user.getRoles().stream().anyMatch(role ->
                "ROLE_ADMINISTRADOR".equals(role.getAuthority()))) {
            throw new AccessDeniedException("Usuários com a role ROLE_ADMINISTRADOR não podem ser excluídos.");
        }
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException(UserConstants.ID_NOT_FOUND);
            }

        } catch (DataAccessException e) {

            throw new DatabaseException(UserConstants.STATUS_CHANGE_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserPhotoDTO getUserPhotoById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(UserConstants.NULL_ID);
        }

        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException(UserConstants.USER_NOT_FOUND);
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
