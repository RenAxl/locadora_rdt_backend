package com.locadora_rdt_backend.modules.rentals.rental.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.financial.payment_methods.repository.PaymentMethodRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDTO;
import com.locadora_rdt_backend.modules.organization.customers.mapper.CustomerMapper;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.dto.*;
import com.locadora_rdt_backend.modules.rentals.rental.mapper.RentalMapper;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItemUnit;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItemUnitStatus;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalStatusHistory;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalItemRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalItemUnitRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalStatusHistoryRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalUnitRepository;
import com.locadora_rdt_backend.modules.rentals.rental_types.model.RentalType;
import com.locadora_rdt_backend.modules.rentals.rental_types.repository.RentalTypeRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository repository;
    private final RentalItemRepository itemRepository;
    private final RentalUnitRepository itemUnitRepository;
    private final RentalItemUnitRepository rentalItemUnitRepository;
    private final CustomerRepository customerRepository;
    private final RentalTypeRepository rentalTypeRepository;
    private final ItemRepository inventoryItemRepository;
    private final RentalMapper mapper;
    private final CustomerMapper customerMapper;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;
    private final RentalStockService stockService;
    private final RentalStatusHistoryRepository historyRepository;
    private final RentalDocumentService documentService;

    public RentalServiceImpl(
            RentalRepository repository,
            RentalItemRepository itemRepository,
            RentalUnitRepository itemUnitRepository,
            RentalItemUnitRepository rentalItemUnitRepository,
            CustomerRepository customerRepository,
            RentalTypeRepository rentalTypeRepository,
            ItemRepository inventoryItemRepository,
            RentalMapper mapper,
            CustomerMapper customerMapper,
            PaymentMethodRepository paymentMethodRepository,
            AuthenticationFacade authenticationFacade,
            UserRepository userRepository,
            RentalStockService stockService,
            RentalStatusHistoryRepository historyRepository,
            RentalDocumentService documentService
    ) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.itemUnitRepository = itemUnitRepository;
        this.rentalItemUnitRepository = rentalItemUnitRepository;
        this.customerRepository = customerRepository;
        this.rentalTypeRepository = rentalTypeRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.mapper = mapper;
        this.customerMapper = customerMapper;
        this.paymentMethodRepository = paymentMethodRepository;
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
        this.stockService = stockService;
        this.historyRepository = historyRepository;
        this.documentService = documentService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalDTO> findAllPaged(String number, String customer, String status, Long rentalTypeId,
            Instant dateFrom, Instant dateTo, PageRequest pageRequest) {

        Long typeId = rentalTypeId;
        if (typeId == null) {
            typeId = RentalConstants.FILTER_ID_DISABLED;
        }

        Instant initialDate = dateFrom;
        if (initialDate == null) {
            initialDate = RentalConstants.FILTER_FIRST_DATE;
        }

        Instant finalDate = dateTo;
        if (finalDate == null) {
            finalDate = RentalConstants.FILTER_LAST_DATE;
        }

        String rentalNumber = text(number);
        String customerName = text(customer);
        String rentalStatus = text(status);
        Page<Rental> rentals = repository.find(rentalNumber, customerName, rentalStatus,
                typeId, initialDate, finalDate, pageRequest);
        Page<RentalDTO> rentalsDTO = rentals.map(rental -> {
            List<RentalItem> items = itemRepository.findByRentalIdOrderById(rental.getId());
            rental.setItems(items);
            RentalDTO dto = mapper.toDTO(rental);
            fillLateFee(rental, items, dto);
            return dto;
        });

        return rentalsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public RentalDTO findById(Long id) {

        Rental rental = findEntity(id);
        List<RentalItem> items = itemRepository.findByRentalIdOrderById(id);
        rental.setItems(items);

        RentalDTO rentalDTO = mapper.toDTO(rental);
        fillLateFee(rental, items, rentalDTO);

        return rentalDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO findCurrentCustomer() {

        String username = authenticationFacade.getAuthenticatedUsername();
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new ResourceNotFoundException(RentalConstants.AUTHENTICATED_USER_NOT_FOUND);
        }
        Customer customer = customerRepository.findByEmail(username);
        if (customer == null) {
            throw new ResourceNotFoundException(
                    "O usuário " + user.getName() + RentalConstants.USER_IS_NOT_CUSTOMER_SUFFIX
            );
        }
        if (!Boolean.TRUE.equals(customer.getActive())) {
            throw new IllegalArgumentException(RentalConstants.ACTIVE_CUSTOMER_REQUIRED);
        }

        CustomerDTO customerDTO = customerMapper.toDTO(customer);

        return customerDTO;
    }

    @Override
    @Transactional
    public RentalDTO insert(RentalInsertDTO dto) {

        Rental rental = mapper.toEntity(dto);
        rental.setRentalNumber(generateNumber());
        rental.setStatus(RentalConstants.STATUS_RENTED);
        rental.setRegistrationDate(Instant.now());
        rental.setCreatedBy(authenticationFacade.getAuthenticatedUsername());
        fillRental(rental, dto);
        Rental savedRental = repository.save(rental);
        List<RentalItem> items = saveItems(savedRental, rental.getItems());
        if (items.isEmpty()) {
            throw new IllegalArgumentException(RentalConstants.AT_LEAST_ONE_ITEM_REQUIRED);
        }
        stockService.reserveUnits(savedRental, items);
        registerHistory(savedRental, null, RentalConstants.STATUS_RENTED, RentalConstants.RENTAL_CREATED_HISTORY);
        savedRental.setItems(items);
        RentalDTO rentalDTO = mapper.toDTO(savedRental);
        fillLateFee(savedRental, items, rentalDTO);
        rentalDTO.setMessage(RentalConstants.RESERVATION_SUCCESS);

        return rentalDTO;
    }

    @Override
    @Transactional
    public RentalDTO confirm(Long id) {

        Rental rental = findEntity(id);
        if (!RentalConstants.STATUS_RENTED.equals(rental.getStatus())) {
            throw new IllegalArgumentException(RentalConstants.RENTED_RENTAL_REQUIRED);
        }
        RentalDTO rentalDTO = mapper.toDTO(rental);

        return rentalDTO;
    }

    @Override
    @Transactional
    public RentalDTO start(Long id, RentalCheckoutDTO dto) {

        Rental rental = findEntity(id);
        if (!RentalConstants.STATUS_RENTED.equals(rental.getStatus())) {
            throw new IllegalArgumentException(RentalConstants.DELIVERY_RENTED_RENTAL_REQUIRED);
        }

        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById(dto.getPaymentMethodId());

        if (!paymentMethodOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalConstants.PAYMENT_METHOD_NOT_FOUND);
        }

        PaymentMethod paymentMethod = paymentMethodOptional.get();
        List<RentalItemUnit> units = rentalItemUnitRepository.findByRentalItemRentalIdOrderById(id);
        Instant now = Instant.now();
        List<RentalItem> rentalItems = itemRepository.findByRentalIdOrderById(id);
        stockService.returnReservedUnits(rental, rentalItems, units, now);

        long overdueDays = calculateOverdueDays(rental);
        BigDecimal lateFee = RentalConstants.ZERO;
        if (overdueDays > 0) {
            BigDecimal lateFeePerDay = calculateLateFeePerDay(rental, rentalItems);
            lateFee = money(lateFeePerDay.multiply(BigDecimal.valueOf(overdueDays)));
        }
        BigDecimal discount = RentalConstants.ZERO;
        String paymentName = RentalConstants.EMPTY_TEXT;
        if (paymentMethod.getName() != null) {
            paymentName = paymentMethod.getName().toLowerCase();
        }
        if (lateFee.compareTo(RentalConstants.ZERO) == 0
                && (paymentName.equals(RentalConstants.PAYMENT_PIX) || paymentName.contains(RentalConstants.PAYMENT_BANK_SLIP))) {
            discount = money(rental.getTotalAmount().multiply(RentalConstants.PIX_AND_BANK_SLIP_DISCOUNT));
        }

        mapper.updateEntity(rental, paymentMethod);

        rental.setStatus(RentalConstants.STATUS_DELIVERED);
        rental.setEffectiveReturnDate(now);
        rental.setPaid(true);
        rental.setLateFee(lateFee);
        rental.setDiscount(discount);
        rental.setRemainingAmount(money(rental.getTotalAmount().add(lateFee).subtract(discount)));
        rental.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        Rental savedRental = repository.save(rental);
        registerHistory(savedRental, RentalConstants.STATUS_RENTED, RentalConstants.STATUS_DELIVERED, RentalConstants.RENTAL_DELIVERED_HISTORY);
        savedRental.setItems(rentalItems);

        RentalDTO rentalDTO = mapper.toDTO(savedRental);
        fillLateFee(savedRental, rentalItems, rentalDTO);
        rentalDTO.setMessage(RentalConstants.CHECKOUT_SUCCESS);

        return rentalDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemAvailabilityDTO findAvailability(Long itemId) {

        Item item = findActiveItem(itemId);
        long availableQuantity = itemUnitRepository.countAvailableForRental(itemId);
        ItemAvailabilityDTO dto = new ItemAvailabilityDTO();
        dto.setItemId(item.getId());
        dto.setItemName(item.getName());
        dto.setAvailableQuantity(availableQuantity);
        dto.setReservedQuantity(itemUnitRepository.countByItemIdAndRentalStatus(itemId, RentalConstants.STATUS_RESERVED));
        dto.setRentedQuantity(itemUnitRepository.countByItemIdAndRentalStatus(itemId, RentalItemUnitStatus.DELIVERED.name()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemUnitDTO> findAvailableUnits(Long itemId) {

        findActiveItem(itemId);
        List<ItemUnit> units = itemUnitRepository.findAvailableForRental(itemId);
        List<ItemUnitDTO> unitsDTO = new ArrayList<>();
        for (ItemUnit unit : units) {
            unitsDTO.add(mapper.toDTO(unit));
        }
        return unitsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemUnitDTO> findItemUnits(Long itemId) {

        findActiveItem(itemId);
        List<ItemUnit> units = itemUnitRepository.findByItemIdOrderByAssetCode(itemId);
        List<ItemUnitDTO> unitsDTO = new ArrayList<>();
        for (ItemUnit unit : units) {
            unitsDTO.add(mapper.toDTO(unit));
        }
        return unitsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalItemUnitDTO> findRentalUnits(Long rentalId) {

        findEntity(rentalId);
        List<RentalItemUnit> units = rentalItemUnitRepository.findByRentalItemRentalIdOrderById(rentalId);
        List<RentalItemUnitDTO> unitsDTO = new ArrayList<>();
        for (RentalItemUnit unit : units) {
            unitsDTO.add(mapper.toDTO(unit));
        }
        return unitsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalStatusHistoryDTO> findHistory(Long rentalId) {

        findEntity(rentalId);
        List<RentalStatusHistory> history = historyRepository.findByRentalIdOrderByChangedAtAsc(rentalId);
        List<RentalStatusHistoryDTO> historyDTO = new ArrayList<>();

        for (RentalStatusHistory entry : history) {
            historyDTO.add(mapper.toDTO(entry));
        }

        return historyDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] receipt(Long id) {

        byte[] document = documentService.receipt(id);

        return document;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] fiscalCoupon(Long id) {

        byte[] document = documentService.fiscalCoupon(id);

        return document;
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Rental rental = findEntity(id);
        List<RentalItem> rentalItems = itemRepository.findByRentalIdOrderById(id);
        List<RentalItemUnit> linkedUnits = rentalItemUnitRepository.findByRentalItemRentalIdOrderById(id);
        stockService.releaseUnitsForDeletion(rental, rentalItems, linkedUnits);

        rentalItemUnitRepository.deleteByRentalItemRentalId(id);
        rentalItemUnitRepository.flush();
        itemRepository.deleteByRentalId(id);
        historyRepository.deleteByRentalId(id);
        repository.delete(rental);
        repository.flush();
    }

    private void fillRental(Rental rental, RentalInsertDTO dto) {

        Customer customer = findAuthenticatedCustomer();
        Optional<RentalType> rentalTypeOptional = rentalTypeRepository.findById(dto.getRentalTypeId());

        if (!rentalTypeOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalConstants.RENTAL_TYPE_NOT_FOUND);
        }

        RentalType rentalType = rentalTypeOptional.get();
        if (!Boolean.TRUE.equals(rentalType.getActive())) {
            throw new IllegalArgumentException(RentalConstants.ACTIVE_RENTAL_TYPE_REQUIRED);
        }
        if (dto.getReturnForecastDate().isBefore(dto.getRentalStartDate())) {
            throw new IllegalArgumentException(RentalConstants.RETURN_FORECAST_DATE_INVALID);
        }

        rental.setCustomer(customer);
        rental.setRentalType(rentalType);
        rental.setPaymentMethod(null);
        rental.setShippingFee(money(dto.getShippingFee()));
        rental.setAdditionalFee(money(dto.getAdditionalFee()));
        rental.setDownPayment(money(dto.getDownPayment()));
        rental.setLateFee(RentalConstants.ZERO);
        rental.setDamageFee(RentalConstants.ZERO);

        List<RentalItem> items = createItems(rental, dto.getItems());
        rental.setItems(items);
        BigDecimal subtotal = RentalConstants.ZERO;
        for (RentalItem item : items) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        subtotal = money(subtotal);
        rental.setDiscount(RentalConstants.ZERO);
        BigDecimal total = subtotal.subtract(rental.getDiscount());
        total = total.add(rental.getShippingFee());
        total = total.add(rental.getAdditionalFee());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(RentalConstants.NEGATIVE_RENTAL_TOTAL);
        }
        rental.setSubtotal(subtotal);
        rental.setTotalAmount(total);
        BigDecimal remainingAmount = total.subtract(rental.getDownPayment());
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }
        rental.setRemainingAmount(remainingAmount);
    }

    private List<RentalItem> createItems(Rental rental, List<RentalItemInsertDTO> dtos) {

        List<RentalItem> items = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        Integer rentalDays = rental.getRentalType().getDays();
        if (rentalDays == null || rentalDays <= 0) {
            throw new IllegalArgumentException(RentalConstants.INVALID_RENTAL_DAYS);
        }
        if (dtos == null) {
            return items;
        }
        for (RentalItemInsertDTO dto : dtos) {
            if (ids.contains(dto.getItemId())) {
                throw new IllegalArgumentException(RentalConstants.DUPLICATE_RENTAL_ITEM);
            }
            ids.add(dto.getItemId());
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                throw new IllegalArgumentException(RentalConstants.INVALID_ITEM_QUANTITY);
            }
            Item item = findActiveItem(dto.getItemId());
            RentalItem entity = new RentalItem();
            entity.setRental(rental);
            entity.setItem(item);
            entity.setQuantity(dto.getQuantity());
            BigDecimal unitPrice = money(item.getPrice());
            entity.setUnitPrice(unitPrice);
            entity.setDiscount(money(dto.getDiscount()));
            entity.setAdditionalFee(money(dto.getAdditionalFee()));
            BigDecimal subtotal = calculateItemSubtotal(entity.getUnitPrice(), entity.getQuantity(),
                    rentalDays, entity.getDiscount(), entity.getAdditionalFee());
            entity.setSubtotal(subtotal);
            items.add(entity);
        }
        return items;
    }

    private List<RentalItem> saveItems(Rental rental, List<RentalItem> items) {

        List<RentalItem> savedItems = new ArrayList<>();
        for (RentalItem entity : items) {
            entity.setRental(rental);
            RentalItem savedItem = itemRepository.save(entity);
            savedItems.add(savedItem);
        }
        return savedItems;
    }

    private BigDecimal calculateItemSubtotal(BigDecimal unitPrice, Integer quantity,
            Integer rentalDays, BigDecimal discount, BigDecimal additionalFee) {

        BigDecimal price = money(unitPrice);
        BigDecimal itemQuantity = BigDecimal.valueOf(quantity);
        BigDecimal days = BigDecimal.valueOf(rentalDays);
        BigDecimal subtotal = price.multiply(itemQuantity).multiply(days);
        subtotal = subtotal.subtract(money(discount));
        subtotal = subtotal.add(money(additionalFee));

        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(RentalConstants.NEGATIVE_ITEM_SUBTOTAL);
        }

        return money(subtotal);
    }

    private Rental findEntity(Long id) {

        Optional<Rental> rentalOptional = repository.findById(id);

        if (!rentalOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalConstants.RENTAL_NOT_FOUND);
        }
        Rental rental = rentalOptional.get();

        return rental;
    }

    private Customer findAuthenticatedCustomer() {

        String email = authenticationFacade.getAuthenticatedUsername();
        Customer customer = customerRepository.findByEmail(email);

        if (customer == null) {
            throw new ResourceNotFoundException(
                    RentalConstants.AUTHENTICATED_CUSTOMER_NOT_FOUND
            );
        }

        if (!Boolean.TRUE.equals(customer.getActive())) {
            throw new IllegalArgumentException(RentalConstants.ACTIVE_CUSTOMER_REQUIRED);
        }

        return customer;
    }

    private Item findActiveItem(Long id) {

        Optional<Item> itemOptional = inventoryItemRepository.findById(id);
        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalConstants.ITEM_NOT_FOUND);
        }
        Item item = itemOptional.get();
        if (!Boolean.TRUE.equals(item.getActive())) {
            throw new IllegalArgumentException(RentalConstants.ACTIVE_ITEM_REQUIRED);
        }
        return item;
    }

    private BigDecimal money(BigDecimal value) {

        if (value == null) {
            return RentalConstants.ZERO;
        }
        return value.setScale(RentalConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String text(String value) {

        if (value == null) {
            return RentalConstants.EMPTY_TEXT;
        }
        return value.trim();
    }

    private String generateNumber() {

        DateTimeFormatter formatter = RentalConstants.RENTAL_NUMBER_FORMATTER.withZone(ZoneOffset.UTC);
        String date = formatter.format(Instant.now());
        return RentalConstants.RENTAL_NUMBER_PREFIX + date;
    }

    private void fillLateFee(Rental rental, List<RentalItem> items, RentalDTO dto) {

        dto.setOverdueDays(0L);
        dto.setLateFeePerDay(RentalConstants.ZERO);
        dto.setCalculatedLateFee(RentalConstants.ZERO);
        dto.setTotalWithLateFee(money(rental.getTotalAmount()));

        long overdueDays = calculateOverdueDays(rental);
        if (overdueDays == 0) {
            return;
        }

        BigDecimal totalAmount = money(rental.getTotalAmount());
        BigDecimal lateFeePerDay = calculateLateFeePerDay(rental, items);
        BigDecimal calculatedLateFee = lateFeePerDay.multiply(BigDecimal.valueOf(overdueDays));
        calculatedLateFee = money(calculatedLateFee);

        dto.setOverdueDays(overdueDays);
        dto.setLateFeePerDay(lateFeePerDay);
        dto.setCalculatedLateFee(calculatedLateFee);
        dto.setTotalWithLateFee(money(totalAmount.add(calculatedLateFee)));
    }

    private long calculateOverdueDays(Rental rental) {

        if (rental.getReturnForecastDate() == null) {
            return 0L;
        }

        LocalDate expectedDate = LocalDate.ofInstant(rental.getReturnForecastDate(), ZoneId.systemDefault());
        LocalDate returnDate = LocalDate.now();
        if (rental.getEffectiveReturnDate() != null) {
            returnDate = LocalDate.ofInstant(rental.getEffectiveReturnDate(), ZoneId.systemDefault());
        }

        if (!expectedDate.isBefore(returnDate)) {
            return 0L;
        }

        return ChronoUnit.DAYS.between(expectedDate, returnDate);
    }

    private BigDecimal calculateLateFeePerDay(Rental rental, List<RentalItem> items) {

        BigDecimal lateFeePerDay = calculateItemsDailyValue(items);
        if (lateFeePerDay.compareTo(RentalConstants.ZERO) == 0) {
            lateFeePerDay = calculateRentalDailyValue(rental);
        }

        return lateFeePerDay;
    }

    private BigDecimal calculateItemsDailyValue(List<RentalItem> items) {

        BigDecimal dailyValue = RentalConstants.ZERO;
        if (items == null) {
            return dailyValue;
        }

        for (RentalItem item : items) {
            BigDecimal unitPrice = money(item.getUnitPrice());
            int quantity = 0;
            if (item.getQuantity() != null) {
                quantity = item.getQuantity();
            }
            dailyValue = dailyValue.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }
        return dailyValue.setScale(RentalConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRentalDailyValue(Rental rental) {

        if (rental.getRentalType() == null || rental.getRentalType().getDays() == null
                || rental.getRentalType().getDays() <= 0) {
            return RentalConstants.ZERO;
        }

        BigDecimal subtotal = money(rental.getSubtotal());
        return subtotal.divide(
                BigDecimal.valueOf(rental.getRentalType().getDays()),
                RentalConstants.MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private void registerHistory(Rental rental, String previousStatus, String newStatus, String reason) {

        String username = authenticationFacade.getAuthenticatedUsername();
        RentalStatusHistory history = new RentalStatusHistory();

        history.setRental(rental);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setReason(reason);
        history.setChangedAt(Instant.now());
        history.setChangedBy(username);
        history.setCreatedBy(username);

        historyRepository.save(history);
    }
}
