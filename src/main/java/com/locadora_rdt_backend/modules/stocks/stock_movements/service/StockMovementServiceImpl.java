package com.locadora_rdt_backend.modules.stocks.stock_movements.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_movements.constants.StockMovementConstants;
import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementInsertDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.mapper.StockMovementMapper;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import com.locadora_rdt_backend.modules.stocks.stock_movements.repository.StockMovementRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository repository;
    private final ItemUnitRepository itemUnitRepository;
    private final ItemRepository itemRepository;
    private final StockMovementMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public StockMovementServiceImpl(StockMovementRepository repository, ItemUnitRepository itemUnitRepository,
                                    ItemRepository itemRepository, StockMovementMapper mapper,
                                    AuthenticationFacade authenticationFacade) {
        this.repository = repository;
        this.itemUnitRepository = itemUnitRepository;
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementDTO> findAllPaged(String name, PageRequest pageRequest) {
        String search = "";
        if (name != null) {
            search = name.trim();
        }
        Page<StockMovement> movements = repository.find(search, pageRequest);
        return movements.map(movement -> mapper.toDTO(movement));
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementDTO findById(Long id) {
        Optional<StockMovement> movement = repository.findById(id);
        if (!movement.isPresent()) {
            throw new ResourceNotFoundException(StockMovementConstants.STOCK_MOVEMENT_NOT_FOUND);
        }
        return mapper.toDTO(movement.get());
    }

    @Override
    @Transactional
    public StockMovementDTO insert(StockMovementInsertDTO dto) {
        validateMovement(dto);
        Optional<Item> itemOptional = itemRepository.findByIdForUpdate(dto.getItemId());
        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }
        Item item = itemOptional.get();
        StockMovement movement = mapper.toEntity(dto);
        movement.setItem(item);
        movement.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        if (dto.getType() == StockMovementType.ENTRY) {
            validateActiveItem(item);
            movement.setItemUnit(createAvailableUnits(item, dto.getQuantity()));
        } else if (dto.getType() == StockMovementType.EXIT) {
            movement.setItemUnit(removeAvailableUnits(item, dto.getQuantity(), dto.getItemUnitId()));
        } else if (dto.getType() == StockMovementType.ADJUSTMENT) {
            applyAdjustment(item, dto.getQuantity());
        } else {
            changeUnitStatus(item, dto, movement);
        }
        return mapper.toDTO(repository.save(movement));
    }

    private void validateMovement(StockMovementInsertDTO dto) {
        if (dto.getType() == null) {
            throw new DatabaseException(StockMovementConstants.INVALID_MOVEMENT_TYPE);
        }
        if (dto.getQuantity() == null || dto.getQuantity() < 0 || !dto.isQuantityValid()) {
            throw new DatabaseException(StockMovementConstants.INVALID_QUANTITY);
        }
        if (dto.getReason() != null && dto.getReason().length() > 255) {
            throw new DatabaseException(StockMovementConstants.REASON_LENGTH);
        }
        if (dto.getType() == StockMovementType.STATUS_CHANGE) {
            if (dto.getItemUnitId() == null || dto.getStatus() == null) {
                throw new DatabaseException(StockMovementConstants.INVALID_UNIT_MOVEMENT);
            }
        } else if (dto.getStatus() != null) {
            throw new DatabaseException(ItemUnitConstants.INVALID_STATUS);
        }
        if (dto.getItemUnitId() != null && dto.getType() != StockMovementType.STATUS_CHANGE
                && (dto.getType() != StockMovementType.EXIT || dto.getQuantity() != 1)) {
            throw new DatabaseException(StockMovementConstants.INVALID_UNIT_MOVEMENT);
        }
    }

    private void applyAdjustment(Item item, int desiredTotal) {
        int currentTotal = Math.toIntExact(itemUnitRepository.countByItemIdAndActiveTrue(item.getId()));
        if (desiredTotal > currentTotal) {
            validateActiveItem(item);
            createAvailableUnits(item, desiredTotal - currentTotal);
        } else if (desiredTotal < currentTotal) {
            removeAvailableUnits(item, currentTotal - desiredTotal, null);
        }
    }

    private ItemUnit createAvailableUnits(Item item, int quantity) {
        String username = authenticationFacade.getAuthenticatedUsername();
        ItemUnit lastCreated = null;
        for (int index = 0; index < quantity; index++) {
            String randomCode = UUID.randomUUID().toString().substring(0, 8);
            ItemUnit unit = new ItemUnit();
            unit.setItem(item);
            unit.setAssetCode(ItemUnitConstants.ASSET_CODE_PREFIX + item.getId() + "-" + randomCode);
            unit.setStatus(ItemUnitStatus.AVAILABLE);
            unit.setConditionStatus(ItemUnitCondition.GOOD);
            unit.setPurchaseDate(LocalDate.now());
            unit.setNotes(StockMovementConstants.MOVEMENT_CREATION_NOTE);
            unit.setActive(true);
            unit.setCreatedBy(username);
            lastCreated = itemUnitRepository.save(unit);
        }
        if (quantity == 1) {
            return lastCreated;
        }
        return null;
    }

    private ItemUnit removeAvailableUnits(Item item, int quantity, Long unitId) {
        List<ItemUnit> units;
        if (unitId != null) {
            ItemUnit unit = findUnit(item, unitId);
            if (!Boolean.TRUE.equals(unit.getActive()) || unit.getStatus() != ItemUnitStatus.AVAILABLE
                    || !isActiveItem(item)) {
                throw new DatabaseException(StockMovementConstants.INSUFFICIENT_STOCK);
            }
            units = Collections.singletonList(unit);
        } else {
            units = itemUnitRepository.findAvailableByItemIdForUpdate(item.getId(), quantity);
        }
        if (units.size() < quantity) {
            throw new DatabaseException(StockMovementConstants.INSUFFICIENT_STOCK);
        }
        String username = authenticationFacade.getAuthenticatedUsername();
        for (ItemUnit unit : units) {
            unit.setActive(false);
            unit.setUpdatedBy(username);
            itemUnitRepository.save(unit);
        }
        if (quantity == 1) {
            return units.get(0);
        }
        return null;
    }

    private void changeUnitStatus(Item item, StockMovementInsertDTO dto, StockMovement movement) {
        ItemUnit unit = findUnit(item, dto.getItemUnitId());
        if (!Boolean.TRUE.equals(unit.getActive())) {
            throw new DatabaseException(ItemUnitConstants.INACTIVE_UNIT);
        }
        if (unit.getStatus() == dto.getStatus()) {
            throw new DatabaseException("A unidade já está no status informado");
        }
        movement.setItemUnit(unit);
        movement.setPreviousStatus(unit.getStatus());
        movement.setNewStatus(dto.getStatus());
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            movement.setReason("Status alterado de " + unit.getStatus() + " para " + dto.getStatus());
        }
        unit.setStatus(dto.getStatus());
        unit.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        itemUnitRepository.save(unit);
    }

    private ItemUnit findUnit(Item item, Long id) {
        Optional<ItemUnit> unit = itemUnitRepository.findByIdForUpdate(id);
        if (!unit.isPresent()) {
            throw new ResourceNotFoundException(ItemUnitConstants.ITEM_UNIT_NOT_FOUND);
        }
        if (!item.getId().equals(unit.get().getItem().getId())) {
            throw new DatabaseException(StockMovementConstants.UNIT_ITEM_MISMATCH);
        }
        return unit.get();
    }

    private boolean isActiveItem(Item item) {
        return Boolean.TRUE.equals(item.getActive())
                && item.getCategory() != null && Boolean.TRUE.equals(item.getCategory().getActive());
    }

    private void validateActiveItem(Item item) {
        if (!isActiveItem(item)) {
            throw new DatabaseException(ItemUnitConstants.INACTIVE_ITEM);
        }
    }
}
