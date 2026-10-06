package com.locadora_rdt_backend.modules.stocks.item_units.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitInsertDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitStatusUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.mapper.ItemUnitMapper;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import com.locadora_rdt_backend.modules.stocks.stock_movements.repository.StockMovementRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemUnitServiceImpl implements ItemUnitService {

    private final ItemUnitRepository repository;
    private final ItemUnitMapper mapper;
    private final ItemRepository itemRepository;
    private final AuthenticationFacade authenticationFacade;
    private final StockMovementRepository stockMovementRepository;

    public ItemUnitServiceImpl(ItemUnitRepository repository, ItemUnitMapper mapper, ItemRepository itemRepository,
                               AuthenticationFacade authenticationFacade, StockMovementRepository stockMovementRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.authenticationFacade = authenticationFacade;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemUnitDTO> findAllPaged(String name, Long itemId, Boolean active, PageRequest pageRequest) {
        String search = "";
        if (name != null) {
            search = name.trim();
        }
        Long itemFilter = -1L;
        if (itemId != null) {
            itemFilter = itemId;
        }
        if (itemFilter != -1L && !itemRepository.existsById(itemFilter)) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }
        int activeFilter = -1;
        if (active != null) {
            activeFilter = active ? 1 : 0;
        }
        Page<ItemUnit> units = repository.find(search, itemFilter, activeFilter, pageRequest);
        return units.map(unit -> mapper.toDTO(unit));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemUnitDTO findById(Long id) {
        Optional<ItemUnit> unit = repository.findById(id);
        if (!unit.isPresent()) {
            throw new ResourceNotFoundException(ItemUnitConstants.ITEM_UNIT_NOT_FOUND);
        }
        return mapper.toDTO(unit.get());
    }

    @Override
    @Transactional
    public ItemUnitDTO insert(ItemUnitInsertDTO dto) {
        Optional<Item> item = itemRepository.findByIdForUpdate(dto.getItemId());
        if (!item.isPresent()) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }
        validateActiveItem(item.get());
        if (dto.getConditionStatus() == null) {
            throw new DatabaseException(ItemUnitConstants.FIELD_REQUIRED);
        }
        ItemUnit unit = mapper.toEntity(dto);
        unit.setItem(item.get());
        String randomCode = UUID.randomUUID().toString().substring(0, 8);
        unit.setAssetCode(ItemUnitConstants.ASSET_CODE_PREFIX + item.get().getId() + "-" + randomCode);
        unit.setCreatedBy(authenticationFacade.getAuthenticatedUsername());
        try {
            ItemUnit saved = repository.saveAndFlush(unit);
            recordMovement(saved, StockMovementType.ENTRY, "Cadastro da unidade " + saved.getAssetCode(), null);
            return mapper.toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(ItemUnitConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public ItemUnitDTO update(Long id, ItemUnitUpdateDTO dto) {
        ItemUnit unit = findUnitForUpdate(id);
        if (!unit.getItem().getId().equals(dto.getItemId())) {
            throw new DatabaseException(ItemUnitConstants.ITEM_CANNOT_BE_CHANGED);
        }
        if (dto.getConditionStatus() == null) {
            throw new DatabaseException(ItemUnitConstants.FIELD_REQUIRED);
        }
        ItemUnitCondition previousCondition = unit.getConditionStatus();
        mapper.updateEntity(unit, dto);
        unit.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        try {
            ItemUnit saved = repository.saveAndFlush(unit);
            if (previousCondition != saved.getConditionStatus()) {
                String reason = "Condição alterada de " + previousCondition + " para " + saved.getConditionStatus();
                recordMovement(saved, StockMovementType.STATUS_CHANGE, reason, saved.getStatus());
            }
            return mapper.toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(ItemUnitConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // A baixa preserva a unidade e suas movimentações antigas.
        changeUnitActive(findUnitForUpdate(id), false);
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.contains(null)) {
            throw new DatabaseException(ItemUnitConstants.EMPTY_ID_LIST);
        }
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new DatabaseException(ItemUnitConstants.DUPLICATE_IDS);
        }
        // Bloqueia os itens na mesma ordem antes de bloquear as unidades.
        itemRepository.findByItemUnitIdsForUpdate(ids);
        List<ItemUnit> units = new ArrayList<>();
        for (Long id : ids) {
            units.add(findLockedUnit(id));
        }
        for (ItemUnit unit : units) {
            changeUnitActive(unit, false);
        }
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {
        changeUnitActive(findUnitForUpdate(id), active);
    }

    @Override
    @Transactional
    public ItemUnitDTO updateStatus(Long id, ItemUnitStatusUpdateDTO dto) {
        ItemUnit unit = findUnitForUpdate(id);
        changeUnitStatus(unit, dto.getStatus(), dto.getReason());
        return mapper.toDTO(unit);
    }

    @Override
    @Transactional
    public ItemUnitDTO changeMaintenanceStatus(Long id, boolean maintenance) {
        ItemUnit unit = findUnitForUpdate(id);
        if (unit.getStatus() != ItemUnitStatus.AVAILABLE && unit.getStatus() != ItemUnitStatus.MAINTENANCE) {
            throw new DatabaseException(ItemUnitConstants.INVALID_MAINTENANCE_CHANGE);
        }
        ItemUnitStatus status = ItemUnitStatus.AVAILABLE;
        if (maintenance) {
            status = ItemUnitStatus.MAINTENANCE;
        }
        changeUnitStatus(unit, status, "Alteração de manutenção da unidade " + unit.getAssetCode());
        return mapper.toDTO(unit);
    }

    private void changeUnitActive(ItemUnit unit, boolean active) {
        if (Boolean.valueOf(active).equals(unit.getActive())) {
            return;
        }
        StockMovementType type = StockMovementType.EXIT;
        String reason = "Baixa da unidade " + unit.getAssetCode();
        if (active) {
            validateActiveItem(unit.getItem());
            type = StockMovementType.ENTRY;
            reason = "Reativação da unidade " + unit.getAssetCode();
        }
        unit.setActive(active);
        unit.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        repository.saveAndFlush(unit);
        recordMovement(unit, type, reason, unit.getStatus());
    }

    private void changeUnitStatus(ItemUnit unit, ItemUnitStatus status, String reason) {
        if (status == null) {
            throw new DatabaseException(ItemUnitConstants.INVALID_STATUS);
        }
        if (reason != null && reason.length() > 255) {
            throw new DatabaseException(ItemUnitConstants.REASON_LENGTH);
        }
        if (!Boolean.TRUE.equals(unit.getActive())) {
            throw new DatabaseException(ItemUnitConstants.INACTIVE_UNIT);
        }
        ItemUnitStatus previousStatus = unit.getStatus();
        if (previousStatus == status) {
            return;
        }
        unit.setStatus(status);
        unit.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        repository.saveAndFlush(unit);
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Status alterado de " + previousStatus + " para " + status;
        }
        recordMovement(unit, StockMovementType.STATUS_CHANGE, reason, previousStatus);
    }

    private void recordMovement(ItemUnit unit, StockMovementType type, String reason, ItemUnitStatus previousStatus) {
        StockMovement movement = new StockMovement();
        movement.setItem(unit.getItem());
        movement.setItemUnit(unit);
        movement.setType(type);
        movement.setQuantity(1);
        movement.setReason(reason);
        movement.setPreviousStatus(previousStatus);
        movement.setNewStatus(unit.getStatus());
        movement.setCreatedBy(authenticationFacade.getAuthenticatedUsername());
        stockMovementRepository.save(movement);
    }

    private ItemUnit findUnitForUpdate(Long id) {
        // Todas as alterações físicas bloqueiam primeiro o item, depois a unidade.
        itemRepository.findByItemUnitIdForUpdate(id);
        return findLockedUnit(id);
    }

    private ItemUnit findLockedUnit(Long id) {
        Optional<ItemUnit> unit = repository.findByIdForUpdate(id);
        if (!unit.isPresent()) {
            throw new ResourceNotFoundException(ItemUnitConstants.ITEM_UNIT_NOT_FOUND);
        }
        return unit.get();
    }

    private void validateActiveItem(Item item) {
        if (!Boolean.TRUE.equals(item.getActive())
                || (item.getCategory() != null && !Boolean.TRUE.equals(item.getCategory().getActive()))) {
            throw new DatabaseException(ItemUnitConstants.INACTIVE_ITEM);
        }
    }
}
