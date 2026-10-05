package com.locadora_rdt_backend.modules.stocks.item_units.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitInsertDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.mapper.ItemUnitMapper;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemUnitServiceImpl implements ItemUnitService {

    private final ItemUnitRepository repository;
    private final ItemUnitMapper mapper;
    private final ItemRepository itemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final AuthenticationFacade authenticationFacade;

    public ItemUnitServiceImpl(
            ItemUnitRepository repository,
            ItemUnitMapper mapper,
            ItemRepository itemRepository,
            StockBalanceRepository stockBalanceRepository,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemUnitDTO> findAllPaged(String name, Long itemId, PageRequest pageRequest) {

        String nameFilter = "";

        if (name != null) {
            nameFilter = name.trim();
        }

        if (itemId != null && itemId != -1L && !itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }

        Long itemFilter = -1L;

        if (itemId != null) {
            itemFilter = itemId;
        }

        Page<ItemUnit> units = repository.find(nameFilter, itemFilter, pageRequest);

        Page<ItemUnitDTO> unitsDTO = units.map(unit -> mapper.toDTO(unit));

        return unitsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemUnitDTO findById(Long id) {

        Optional<ItemUnit> unitOptional = repository.findById(id);

        if (!unitOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemUnitConstants.ITEM_UNIT_NOT_FOUND);
        }

        ItemUnit unit = unitOptional.get();

        ItemUnitDTO unitDTO = mapper.toDTO(unit);

        return unitDTO;
    }

    @Override
    @Transactional
    public ItemUnitDTO insert(ItemUnitInsertDTO dto) {

        Optional<Item> itemOptional = itemRepository.findById(dto.getItemId());

        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }

        Item item = itemOptional.get();
        stockBalanceRepository.findByItemIdForUpdate(item.getId());

        ItemUnit unit = mapper.toEntity(dto);
        unit.setItem(item);
        unit.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        try {

            ItemUnit savedUnit = repository.saveAndFlush(unit);
            synchronizeBalance(item);

            ItemUnitDTO unitDTO = mapper.toDTO(savedUnit);

            return unitDTO;

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

        mapper.updateEntity(unit, dto);
        unit.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        try {

            ItemUnit savedUnit = repository.saveAndFlush(unit);

            ItemUnitDTO unitDTO = mapper.toDTO(savedUnit);

            return unitDTO;

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(ItemUnitConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {

        ItemUnit unit = findUnitForUpdate(id);
        validateAvailableUnit(unit);

        try {

            repository.delete(unit);
            repository.flush();
            synchronizeBalance(unit.getItem());

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(ItemUnitConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new DatabaseException(ItemUnitConstants.EMPTY_ID_LIST);
        }

        List<ItemUnit> units = new ArrayList<>();
        List<Long> existingIds = new ArrayList<>();

        for (Long id : ids) {
            if (!existingIds.contains(id)) {
                ItemUnit unit = findUnitForUpdate(id);
                validateAvailableUnit(unit);
                units.add(unit);
                existingIds.add(id);
            }
        }

        if (existingIds.size() != ids.size()) {
            throw new DatabaseException(ItemUnitConstants.DUPLICATE_IDS);
        }

        try {

            repository.deleteAll(units);
            repository.flush();

            List<Long> updatedItemIds = new ArrayList<>();

            for (ItemUnit unit : units) {
                Item item = unit.getItem();

                if (!updatedItemIds.contains(item.getId())) {
                    synchronizeBalance(item);
                    updatedItemIds.add(item.getId());
                }
            }

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(ItemUnitConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        ItemUnit unit = findUnitForUpdate(id);
        validateAvailableUnit(unit);

        unit.setActive(active);
        unit.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        repository.saveAndFlush(unit);
        synchronizeBalance(unit.getItem());
    }

    private ItemUnit findUnitForUpdate(Long id) {

        // Usa a mesma ordem de bloqueio da edição de saldo: saldo, depois unidade.
        stockBalanceRepository.findByItemUnitIdForUpdate(id);

        Optional<ItemUnit> unitOptional = repository.findByIdForUpdate(id);

        if (!unitOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemUnitConstants.ITEM_UNIT_NOT_FOUND);
        }

        return unitOptional.get();
    }

    private void validateAvailableUnit(ItemUnit unit) {

        if (!ItemUnitConstants.STATUS_AVAILABLE.equals(unit.getStatus())) {
            throw new DatabaseException(ItemUnitConstants.ONLY_AVAILABLE_UNITS_CAN_BE_REMOVED);
        }
    }

    private void synchronizeBalance(Item item) {

        Optional<StockBalance> balanceOptional = stockBalanceRepository.findByItemId(item.getId());
        StockBalance balance;

        if (balanceOptional.isPresent()) {
            balance = balanceOptional.get();
            balance.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        } else {
            balance = new StockBalance();
            balance.setItem(item);
            balance.setMinimumQuantity(0);
            balance.setCreatedBy(authenticationFacade.getAuthenticatedUsername());
        }

        balance.setTotalQuantity((int) repository.countByItemIdAndActiveTrue(item.getId()));
        balance.setReservedQuantity((int) repository.countByItemIdAndStatusAndActiveTrue(
                item.getId(), ItemUnitConstants.STATUS_RESERVED
        ));
        balance.setUnavailableQuantity((int) repository.countUnavailableByItemId(item.getId()));

        stockBalanceRepository.saveAndFlush(balance);
    }

}
