package com.locadora_rdt_backend.modules.stocks.stock_balances.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.items.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.constants.StockBalanceConstants;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.*;
import com.locadora_rdt_backend.modules.stocks.stock_balances.mapper.StockBalanceMapper;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockBalanceServiceImpl implements StockBalanceService {

    private final StockBalanceRepository repository;
    private final StockBalanceMapper mapper;
    private final AuthenticationFacade authenticationFacade;
    private final ItemUnitRepository itemUnitRepository;

    public StockBalanceServiceImpl(
            StockBalanceRepository repository,
            StockBalanceMapper mapper,
            AuthenticationFacade authenticationFacade,
            ItemUnitRepository itemUnitRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
        this.itemUnitRepository = itemUnitRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockBalanceDTO> findAllPaged(String name, PageRequest pageRequest) {

        String nameFilter = "";

        if (name != null) {
            nameFilter = name.trim();
        }

        PageRequest normalizedPageRequest = normalizePageRequest(pageRequest);

        Page<StockBalance> stockBalances = repository.find(nameFilter, normalizedPageRequest);

        for (StockBalance stockBalance : stockBalances.getContent()) {
            synchronizeBalance(stockBalance);
        }

        Page<StockBalanceDTO> stockBalancesDTO = stockBalances.map(stockBalance -> mapper.toDTO(stockBalance));

        Page<StockBalanceDTO> stockBalancesPage = new PageImpl<>(
                stockBalancesDTO.getContent(), pageRequest, stockBalancesDTO.getTotalElements()
        );

        return stockBalancesPage;
    }

    @Override
    @Transactional(readOnly = true)
    public StockBalanceDTO findById(Long id) {

        Optional<StockBalance> stockBalanceOptional = repository.findById(id);

        if (!stockBalanceOptional.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }

        StockBalance stockBalance = stockBalanceOptional.get();

        synchronizeBalance(stockBalance);

        StockBalanceDTO stockBalanceDTO = mapper.toDTO(stockBalance);

        return stockBalanceDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public StockBalanceDTO findByItemId(Long itemId) {

        Optional<StockBalance> stockBalanceOptional = repository.findByItemId(itemId);

        if (!stockBalanceOptional.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }

        StockBalance stockBalance = stockBalanceOptional.get();

        synchronizeBalance(stockBalance);

        StockBalanceDTO stockBalanceDTO = mapper.toDTO(stockBalance);

        return stockBalanceDTO;
    }

    @Override
    @Transactional
    public StockBalanceDTO updateMinimum(Long id, StockBalanceMinimumUpdateDTO dto) {

        Optional<StockBalance> stockBalanceOptional = repository.findByIdForUpdate(id);

        if (!stockBalanceOptional.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }

        StockBalance stockBalance = stockBalanceOptional.get();

        mapper.updateEntity(stockBalance, dto);

        stockBalance.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        synchronizeBalance(stockBalance);

        StockBalance savedStockBalance = repository.save(stockBalance);

        StockBalanceDTO stockBalanceDTO = mapper.toDTO(savedStockBalance);

        return stockBalanceDTO;
    }

    @Override
    @Transactional
    public StockBalanceDTO update(Long id, StockBalanceUpdateDTO dto) {

        Optional<StockBalance> stockBalanceOptional = repository.findByIdForUpdate(id);

        if (!stockBalanceOptional.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }

        StockBalance stockBalance = stockBalanceOptional.get();

        validateQuantities(dto);
        updatePhysicalUnits(stockBalance, dto);

        mapper.updateEntity(stockBalance, dto);

        synchronizeBalance(stockBalance);

        stockBalance.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        StockBalance savedStockBalance = repository.save(stockBalance);

        StockBalanceDTO stockBalanceDTO = mapper.toDTO(savedStockBalance);

        return stockBalanceDTO;
    }

    private void validateQuantities(StockBalanceUpdateDTO dto) {

        int usedQuantity = dto.getReservedQuantity() + dto.getUnavailableQuantity();

        if (usedQuantity > dto.getTotalQuantity()) {
            throw new IllegalArgumentException(StockBalanceConstants.USED_QUANTITY_EXCEEDS_TOTAL);
        }
    }

    private void updatePhysicalUnits(StockBalance stockBalance, StockBalanceUpdateDTO dto) {

        Long itemId = stockBalance.getItem().getId();
        List<ItemUnit> units = itemUnitRepository.findActiveByItemIdForUpdate(itemId);
        int currentTotal = units.size();

        if (dto.getTotalQuantity() > currentTotal) {
            createUnits(stockBalance, dto.getTotalQuantity() - currentTotal);
            units = itemUnitRepository.findActiveByItemIdForUpdate(itemId);
        }

        updateReservedUnits(itemId, units, dto.getReservedQuantity());
        updateUnavailableUnits(units, dto.getUnavailableQuantity());

        units = itemUnitRepository.findActiveByItemIdForUpdate(itemId);
        int quantityToRemove = units.size() - dto.getTotalQuantity();

        if (quantityToRemove > 0) {
            deactivateAvailableUnits(units, quantityToRemove);
        }
    }

    private void createUnits(StockBalance stockBalance, int quantity) {

        String username = authenticationFacade.getAuthenticatedUsername();

        for (int index = 0; index < quantity; index++) {
            String randomCode = UUID.randomUUID().toString().substring(
                    StockBalanceConstants.ASSET_CODE_RANDOM_START, StockBalanceConstants.ASSET_CODE_RANDOM_END
            );
            String assetCode = StockBalanceConstants.ASSET_CODE_PREFIX + stockBalance.getItem().getId()
                    + StockBalanceConstants.ASSET_CODE_SEPARATOR + randomCode;

            ItemUnit unit = new ItemUnit();
            unit.setItem(stockBalance.getItem());
            unit.setAssetCode(assetCode);
            unit.setStatus(StockBalanceConstants.STATUS_AVAILABLE);
            unit.setConditionStatus(StockBalanceConstants.CONDITION_GOOD);
            unit.setPurchaseDate(LocalDate.now());
            unit.setNotes(StockBalanceConstants.MANUAL_CREATION_NOTE);
            unit.setActive(true);
            unit.setCreatedBy(username);

            itemUnitRepository.save(unit);
        }
    }

    private void updateReservedUnits(Long itemId, List<ItemUnit> units, int desiredQuantity) {

        int currentQuantity = countStatus(units, StockBalanceConstants.STATUS_RESERVED);

        if (desiredQuantity > currentQuantity) {
            changeAvailableStatus(units, StockBalanceConstants.STATUS_RESERVED, desiredQuantity - currentQuantity);
            return;
        }

        if (desiredQuantity < currentQuantity) {
            releaseReservedUnits(itemId, currentQuantity - desiredQuantity);
        }
    }

    private void updateUnavailableUnits(List<ItemUnit> units, int desiredQuantity) {

        int currentQuantity = countUnavailable(units);

        if (desiredQuantity > currentQuantity) {
            changeAvailableStatus(units, StockBalanceConstants.STATUS_MAINTENANCE, desiredQuantity - currentQuantity);
            return;
        }

        if (desiredQuantity < currentQuantity) {
            releaseMaintenanceUnits(units, currentQuantity - desiredQuantity);
        }
    }

    private void changeAvailableStatus(List<ItemUnit> units, String newStatus, int quantity) {

        int changed = 0;
        String username = authenticationFacade.getAuthenticatedUsername();

        for (ItemUnit unit : units) {
            if (changed < quantity && StockBalanceConstants.STATUS_AVAILABLE.equals(unit.getStatus())) {
                unit.setStatus(newStatus);
                unit.setUpdatedBy(username);
                itemUnitRepository.save(unit);
                changed++;
            }
        }

        if (changed < quantity) {
            throw new IllegalArgumentException(StockBalanceConstants.INSUFFICIENT_AVAILABLE_UNITS);
        }
    }

    private void releaseReservedUnits(Long itemId, int quantity) {

        List<ItemUnit> units = itemUnitRepository.findByStatusForUpdate(
                itemId, StockBalanceConstants.STATUS_RESERVED, quantity, 0L
        );

        if (units.size() < quantity) {
            throw new IllegalArgumentException(StockBalanceConstants.ACTIVE_RENTAL_UNITS_CANNOT_BE_RELEASED);
        }

        String username = authenticationFacade.getAuthenticatedUsername();

        for (ItemUnit unit : units) {
            unit.setStatus(StockBalanceConstants.STATUS_AVAILABLE);
            unit.setUpdatedBy(username);
            itemUnitRepository.save(unit);
        }
    }

    private void releaseMaintenanceUnits(List<ItemUnit> units, int quantity) {

        int changed = 0;
        String username = authenticationFacade.getAuthenticatedUsername();

        for (ItemUnit unit : units) {
            if (changed < quantity && StockBalanceConstants.STATUS_MAINTENANCE.equals(unit.getStatus())) {
                unit.setStatus(StockBalanceConstants.STATUS_AVAILABLE);
                unit.setUpdatedBy(username);
                itemUnitRepository.save(unit);
                changed++;
            }
        }

        if (changed < quantity) {
            throw new IllegalArgumentException(StockBalanceConstants.LINKED_UNITS_CANNOT_BE_CHANGED);
        }
    }

    private void deactivateAvailableUnits(List<ItemUnit> units, int quantity) {

        int changed = 0;
        String username = authenticationFacade.getAuthenticatedUsername();

        for (ItemUnit unit : units) {
            if (changed < quantity && StockBalanceConstants.STATUS_AVAILABLE.equals(unit.getStatus())) {
                unit.setActive(false);
                unit.setUpdatedBy(username);
                itemUnitRepository.save(unit);
                changed++;
            }
        }

        if (changed < quantity) {
            throw new IllegalArgumentException(StockBalanceConstants.ONLY_AVAILABLE_UNITS_CAN_BE_REMOVED);
        }
    }

    private int countStatus(List<ItemUnit> units, String status) {

        int quantity = 0;

        for (ItemUnit unit : units) {
            if (status.equals(unit.getStatus())) {
                quantity++;
            }
        }

        return quantity;
    }

    private int countUnavailable(List<ItemUnit> units) {

        int quantity = 0;

        for (ItemUnit unit : units) {
            if (!StockBalanceConstants.STATUS_AVAILABLE.equals(unit.getStatus())
                    && !StockBalanceConstants.STATUS_RESERVED.equals(unit.getStatus())) {
                quantity++;
            }
        }

        return quantity;
    }

    private PageRequest normalizePageRequest(PageRequest pageRequest) {

        List<Sort.Order> orders = new ArrayList<>();

        for (Sort.Order order : pageRequest.getSort()) {
            String property = order.getProperty();

            if ("name".equals(property)) {
                property = "item.name";
            }

            property = property.replace("item.category.", "category.");
            property = property.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);

            orders.add(order.withProperty(property));
        }

        PageRequest normalizedPageRequest = PageRequest.of(
                pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by(orders)
        );

        return normalizedPageRequest;
    }

    private void synchronizeBalance(StockBalance stockBalance) {

        Long itemId = stockBalance.getItem().getId();
        int totalQuantity = (int) itemUnitRepository.countByItemIdAndActiveTrue(itemId);
        int reservedQuantity = (int) itemUnitRepository.countByItemIdAndStatusAndActiveTrue(
                itemId, StockBalanceConstants.STATUS_RESERVED
        );
        int unavailableQuantity = (int) itemUnitRepository.countUnavailableByItemId(itemId);

        stockBalance.setTotalQuantity(totalQuantity);
        stockBalance.setReservedQuantity(reservedQuantity);
        stockBalance.setUnavailableQuantity(unavailableQuantity);
    }

}
