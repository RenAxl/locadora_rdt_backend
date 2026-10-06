package com.locadora_rdt_backend.modules.stocks.stock_balances.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.StockQuantitySummary;
import com.locadora_rdt_backend.modules.stocks.stock_balances.constants.StockBalanceConstants;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class StockBalanceServiceImpl implements StockBalanceService {

    private final StockBalanceRepository repository;
    private final StockBalanceMapper mapper;
    private final AuthenticationFacade authenticationFacade;
    private final ItemUnitRepository itemUnitRepository;

    public StockBalanceServiceImpl(StockBalanceRepository repository, StockBalanceMapper mapper,
                                   AuthenticationFacade authenticationFacade, ItemUnitRepository itemUnitRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
        this.itemUnitRepository = itemUnitRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockBalanceDTO> findAllPaged(String name, PageRequest pageRequest) {
        String search = "";
        if (name != null) {
            search = name.trim();
        }
        Page<StockBalance> balances = repository.find(search, normalizePageRequest(pageRequest));
        List<StockBalanceDTO> dtos = new ArrayList<>();
        for (StockBalance balance : balances) {
            dtos.add(toDTO(balance));
        }
        return new PageImpl<>(dtos, pageRequest, balances.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public StockBalanceDTO findById(Long id) {
        Optional<StockBalance> balance = repository.findById(id);
        if (!balance.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }
        return toDTO(balance.get());
    }

    @Override
    @Transactional(readOnly = true)
    public StockBalanceDTO findByItemId(Long itemId) {
        Optional<StockBalance> balance = repository.findByItemId(itemId);
        if (!balance.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }
        return toDTO(balance.get());
    }

    @Override
    @Transactional
    public StockBalanceDTO updateMinimum(Long id, StockBalanceMinimumUpdateDTO dto) {
        if (dto.getMinimumQuantity() == null || dto.getMinimumQuantity() < 0) {
            throw new DatabaseException(StockBalanceConstants.MINIMUM_QUANTITY_MINIMUM);
        }
        Optional<StockBalance> balance = repository.findByIdForUpdate(id);
        if (!balance.isPresent()) {
            throw new ResourceNotFoundException(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND);
        }
        StockBalance entity = balance.get();
        mapper.updateEntity(entity, dto);
        entity.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        return toDTO(repository.save(entity));
    }

    private StockBalanceDTO toDTO(StockBalance balance) {
        StockQuantitySummary quantities = itemUnitRepository.summarizeByItemId(balance.getItem().getId());
        return mapper.toDTO(balance, quantities);
    }

    private PageRequest normalizePageRequest(PageRequest pageRequest) {
        Set<String> allowed = Set.of("id", "version", "item.id", "item.name", "category.name",
                "minimumQuantity", "createdAt", "updatedAt", "createdBy", "updatedBy");
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageRequest.getSort()) {
            String property = order.getProperty();
            if ("name".equals(property) || "itemName".equals(property)) {
                property = "item.name";
            }
            if ("itemId".equals(property)) {
                property = "item.id";
            }
            property = property.replace("item.category.", "category.");
            if (!allowed.contains(property)) {
                throw new DatabaseException(StockBalanceConstants.INVALID_SORT);
            }
            property = property.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            orders.add(order.withProperty(property));
        }
        return PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by(orders));
    }
}
