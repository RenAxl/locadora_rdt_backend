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
import com.locadora_rdt_backend.modules.rentals.rental.dto.ItemAvailabilityDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalCheckoutDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalInsertDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalItemInsertDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalItemUnitDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalStatusHistoryDTO;
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
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTests {

    @Mock
    private RentalRepository repository;

    @Mock
    private RentalItemRepository itemRepository;

    @Mock
    private RentalUnitRepository itemUnitRepository;

    @Mock
    private RentalItemUnitRepository rentalItemUnitRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RentalTypeRepository rentalTypeRepository;

    @Mock
    private ItemRepository inventoryItemRepository;

    @Mock
    private RentalMapper mapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalStockService stockService;

    @Mock
    private RentalStatusHistoryRepository historyRepository;

    @Mock
    private RentalDocumentService documentService;

    @InjectMocks
    private RentalServiceImpl service;

    private Rental rental;
    private RentalDTO rentalDTO;
    private Customer customer;
    private RentalType rentalType;
    private Item item;
    private RentalItem rentalItem;
    private ItemUnit itemUnit;
    private RentalItemUnit rentalItemUnit;
    private RentalInsertDTO insertDTO;
    private RentalItemInsertDTO itemInsertDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(2L);
        customer.setName("Joao");
        customer.setEmail("joao@email.com");
        customer.setActive(true);

        rentalType = new RentalType();
        rentalType.setId(3L);
        rentalType.setName("Diaria");
        rentalType.setDays(2);
        rentalType.setActive(true);

        item = new Item();
        item.setId(4L);
        item.setName("Furadeira");
        item.setPrice(new BigDecimal("10.00"));
        item.setActive(true);

        rental = new Rental();
        rental.setId(1L);
        rental.setRentalNumber("LOC-1");
        rental.setCustomer(customer);
        rental.setRentalType(rentalType);
        rental.setStatus(RentalConstants.STATUS_RENTED);
        rental.setRentalStartDate(Instant.now());
        rental.setReturnForecastDate(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        rental.setTotalAmount(new BigDecimal("20.00"));
        rental.setSubtotal(new BigDecimal("20.00"));
        rental.setWhatsappSent(false);

        rentalDTO = new RentalDTO();
        rentalDTO.setId(1L);
        rentalDTO.setRentalNumber("LOC-1");

        rentalItem = new RentalItem();
        rentalItem.setId(5L);
        rentalItem.setRental(rental);
        rentalItem.setItem(item);
        rentalItem.setQuantity(1);
        rentalItem.setUnitPrice(new BigDecimal("10.00"));
        rentalItem.setSubtotal(new BigDecimal("20.00"));

        itemUnit = new ItemUnit();
        itemUnit.setId(6L);
        itemUnit.setItem(item);
        itemUnit.setAssetCode("PAT-1");
        itemUnit.setStatus(ItemUnitStatus.AVAILABLE);
        itemUnit.setConditionStatus(ItemUnitCondition.GOOD);
        itemUnit.setActive(true);

        rentalItemUnit = new RentalItemUnit();
        rentalItemUnit.setId(7L);
        rentalItemUnit.setRentalItem(rentalItem);
        rentalItemUnit.setItemUnit(itemUnit);
        rentalItemUnit.setStatus(RentalItemUnitStatus.RESERVED);

        itemInsertDTO = new RentalItemInsertDTO();
        itemInsertDTO.setItemId(4L);
        itemInsertDTO.setQuantity(1);

        insertDTO = new RentalInsertDTO();
        insertDTO.setRentalTypeId(3L);
        insertDTO.setRentalStartDate(rental.getRentalStartDate());
        insertDTO.setReturnForecastDate(rental.getReturnForecastDate());
        insertDTO.setItems(Collections.singletonList(itemInsertDTO));
    }

    @Test
    void findAllPagedShouldReturnPageOfRentals() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Rental> rentals = new PageImpl<>(Collections.singletonList(rental));

        when(repository.find("LOC-1", "Joao", "", -1L,
                RentalConstants.FILTER_FIRST_DATE, RentalConstants.FILTER_LAST_DATE, pageRequest)).thenReturn(rentals);
        when(itemRepository.findByRentalIdOrderById(1L)).thenReturn(Collections.singletonList(rentalItem));
        when(mapper.toDTO(rental)).thenReturn(rentalDTO);

        Page<RentalDTO> resultado = service.findAllPaged(" LOC-1 ", " Joao ", null, null, null, null, pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("LOC-1", resultado.getContent().get(0).getRentalNumber());
        assertEquals(new BigDecimal("20.00"), resultado.getContent().get(0).getTotalWithLateFee());
        assertEquals(Collections.singletonList(rentalItem), rental.getItems());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("LOC-1", "Joao", "", -1L,
                RentalConstants.FILTER_FIRST_DATE, RentalConstants.FILTER_LAST_DATE, pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("LOC-1", "Joao", "", null, null, null, pageRequest));
    }

    @Test
    void findByIdShouldReturnRentalWithLateFee() {
        rental.setReturnForecastDate(LocalDate.now().minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(itemRepository.findByRentalIdOrderById(1L)).thenReturn(Collections.singletonList(rentalItem));
        when(mapper.toDTO(rental)).thenReturn(rentalDTO);

        RentalDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(2L, resultado.getOverdueDays());
        assertEquals(new BigDecimal("10.00"), resultado.getLateFeePerDay());
        assertEquals(new BigDecimal("20.00"), resultado.getCalculatedLateFee());
        assertEquals(new BigDecimal("40.00"), resultado.getTotalWithLateFee());
        assertEquals(Collections.singletonList(rentalItem), rental.getItems());
    }

    @Test
    void findByIdShouldThrowExceptionWhenRentalDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findCurrentCustomerShouldReturnCustomer() {
        User user = new User();
        user.setName("Joao");
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(2L);
        customerDTO.setName("Joao");
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(customer);
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        CustomerDTO resultado = service.findCurrentCustomer();

        assertEquals(2L, resultado.getId());
        assertEquals("Joao", resultado.getName());
    }

    @Test
    void findCurrentCustomerShouldThrowExceptionWhenUserDoesNotExist() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.findCurrentCustomer());

        verify(customerRepository, never()).findByEmail(any());
    }

    @Test
    void insertShouldSaveRentalUsingCatalogPriceAndReserveUnits() {
        itemInsertDTO.setDiscount(new BigDecimal("2.00"));
        itemInsertDTO.setAdditionalFee(new BigDecimal("1.00"));
        insertDTO.setShippingFee(new BigDecimal("3.00"));
        insertDTO.setDownPayment(new BigDecimal("5.00"));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(customer);
        when(rentalTypeRepository.findById(3L)).thenReturn(Optional.of(rentalType));
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(rental);
        when(repository.save(rental)).thenReturn(rental);
        when(itemRepository.save(any(RentalItem.class))).thenReturn(rentalItem);
        when(mapper.toDTO(rental)).thenReturn(rentalDTO);

        RentalDTO resultado = service.insert(insertDTO);

        ArgumentCaptor<RentalItem> rentalItemCaptor = ArgumentCaptor.forClass(RentalItem.class);
        verify(itemRepository).save(rentalItemCaptor.capture());
        RentalItem savedItem = rentalItemCaptor.getValue();

        assertEquals(rentalDTO, resultado);
        assertEquals(new BigDecimal("10.00"), savedItem.getUnitPrice());
        assertEquals(new BigDecimal("19.00"), savedItem.getSubtotal());
        assertEquals(new BigDecimal("19.00"), rental.getSubtotal());
        assertEquals(new BigDecimal("22.00"), rental.getTotalAmount());
        assertEquals(new BigDecimal("17.00"), rental.getRemainingAmount());
        assertEquals("joao@email.com", rental.getCreatedBy());
        assertEquals(RentalConstants.RESERVATION_SUCCESS, resultado.getMessage());
        verify(inventoryItemRepository).findById(4L);
        verify(repository).save(rental);
        verify(stockService).reserveUnits(rental, Collections.singletonList(rentalItem));
        verify(historyRepository).save(any(RentalStatusHistory.class));
    }

    @Test
    void insertShouldThrowExceptionWhenItemIsDuplicated() {
        insertDTO.setItems(Arrays.asList(itemInsertDTO, itemInsertDTO));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");
        when(customerRepository.findByEmail("joao@email.com")).thenReturn(customer);
        when(rentalTypeRepository.findById(3L)).thenReturn(Optional.of(rentalType));
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(rental);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.insert(insertDTO));

        assertEquals(RentalConstants.DUPLICATE_RENTAL_ITEM, exception.getMessage());
        verify(repository, never()).save(any());
        verify(stockService, never()).reserveUnits(any(), any());
    }

    @Test
    void confirmShouldReturnRentedRental() {
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(mapper.toDTO(rental)).thenReturn(rentalDTO);

        RentalDTO resultado = service.confirm(1L);

        assertEquals(rentalDTO, resultado);
        verify(repository, never()).save(any());
    }

    @Test
    void confirmShouldThrowExceptionWhenRentalIsDelivered() {
        rental.setStatus(RentalConstants.STATUS_DELIVERED);
        when(repository.findById(1L)).thenReturn(Optional.of(rental));

        assertThrows(IllegalArgumentException.class, () -> service.confirm(1L));

        verify(mapper, never()).toDTO(rental);
    }

    @Test
    void startShouldFinishRentalAndReturnUnits() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(9L);
        paymentMethod.setName("Pix");
        RentalCheckoutDTO checkoutDTO = new RentalCheckoutDTO();
        checkoutDTO.setPaymentMethodId(9L);
        doCallRealMethod().when(mapper).updateEntity(rental, paymentMethod);
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(paymentMethodRepository.findById(9L)).thenReturn(Optional.of(paymentMethod));
        when(rentalItemUnitRepository.findByRentalItemRentalIdOrderById(1L))
                .thenReturn(Collections.singletonList(rentalItemUnit));
        when(itemRepository.findByRentalIdOrderById(1L)).thenReturn(Collections.singletonList(rentalItem));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(rental)).thenReturn(rental);
        when(mapper.toDTO(rental)).thenReturn(rentalDTO);

        RentalDTO resultado = service.start(1L, checkoutDTO);

        assertEquals(rentalDTO, resultado);
        assertEquals(RentalConstants.STATUS_DELIVERED, rental.getStatus());
        assertEquals(true, rental.getPaid());
        assertEquals(paymentMethod, rental.getPaymentMethod());
        assertEquals(new BigDecimal("1.00"), rental.getDiscount());
        assertEquals(new BigDecimal("19.00"), rental.getRemainingAmount());
        assertEquals(RentalConstants.ZERO, rental.getLateFee());
        assertEquals("Usuário Teste", rental.getUpdatedBy());
        assertNotNull(rental.getEffectiveReturnDate());
        assertEquals(false, rental.getWhatsappSent());
        assertEquals(RentalConstants.CHECKOUT_SUCCESS, resultado.getMessage());
        verify(mapper).updateEntity(rental, paymentMethod);
        verify(repository).save(rental);
        verify(stockService).returnReservedUnits(eq(rental), eq(Collections.singletonList(rentalItem)),
                eq(Collections.singletonList(rentalItemUnit)), eq(rental.getEffectiveReturnDate()));
        verify(historyRepository).save(any(RentalStatusHistory.class));
    }

    @Test
    void startShouldThrowExceptionWhenPaymentMethodDoesNotExist() {
        RentalCheckoutDTO checkoutDTO = new RentalCheckoutDTO();
        checkoutDTO.setPaymentMethodId(9L);
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(paymentMethodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.start(1L, checkoutDTO));

        verify(repository, never()).save(any());
        verify(stockService, never()).returnReservedUnits(any(), any(), any(), any());
    }

    @Test
    void findAvailabilityShouldReturnItemQuantities() {
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(itemUnitRepository.countAvailableForRental(4L)).thenReturn(3L);
        when(itemUnitRepository.countByItemIdAndRentalStatus(4L, "RESERVED")).thenReturn(1L);
        when(itemUnitRepository.countByItemIdAndRentalStatus(4L, "DELIVERED")).thenReturn(2L);

        ItemAvailabilityDTO resultado = service.findAvailability(4L);

        assertEquals(4L, resultado.getItemId());
        assertEquals("Furadeira", resultado.getItemName());
        assertEquals(3L, resultado.getAvailableQuantity());
        assertEquals(1L, resultado.getReservedQuantity());
        assertEquals(2L, resultado.getRentedQuantity());
    }

    @Test
    void findAvailabilityShouldThrowExceptionWhenItemDoesNotExist() {
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findAvailability(4L));
    }

    @Test
    void findAvailableUnitsShouldReturnUnits() {
        ItemUnitDTO unitDTO = new ItemUnitDTO();
        unitDTO.setId(6L);
        unitDTO.setAssetCode("PAT-1");
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(itemUnitRepository.findAvailableForRental(4L)).thenReturn(Collections.singletonList(itemUnit));
        when(mapper.toDTO(itemUnit)).thenReturn(unitDTO);

        List<ItemUnitDTO> resultado = service.findAvailableUnits(4L);

        assertEquals(1, resultado.size());
        assertEquals("PAT-1", resultado.get(0).getAssetCode());
    }

    @Test
    void findAvailableUnitsShouldThrowExceptionWhenItemIsInactive() {
        item.setActive(false);
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> service.findAvailableUnits(4L));

        verify(itemUnitRepository, never()).findAvailableForRental(4L);
    }

    @Test
    void findItemUnitsShouldReturnUnits() {
        ItemUnitDTO unitDTO = new ItemUnitDTO();
        unitDTO.setId(6L);
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(itemUnitRepository.findByItemIdOrderByAssetCode(4L)).thenReturn(Collections.singletonList(itemUnit));
        when(mapper.toDTO(itemUnit)).thenReturn(unitDTO);

        List<ItemUnitDTO> resultado = service.findItemUnits(4L);

        assertEquals(1, resultado.size());
        assertEquals(6L, resultado.get(0).getId());
    }

    @Test
    void findItemUnitsShouldThrowExceptionWhenItemDoesNotExist() {
        when(inventoryItemRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findItemUnits(4L));
    }

    @Test
    void findRentalUnitsShouldReturnLinkedUnits() {
        RentalItemUnitDTO unitDTO = new RentalItemUnitDTO();
        unitDTO.setId(7L);
        unitDTO.setAssetCode("PAT-1");
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalItemUnitRepository.findByRentalItemRentalIdOrderById(1L))
                .thenReturn(Collections.singletonList(rentalItemUnit));
        when(mapper.toDTO(rentalItemUnit)).thenReturn(unitDTO);

        List<RentalItemUnitDTO> resultado = service.findRentalUnits(1L);

        assertEquals(1, resultado.size());
        assertEquals(7L, resultado.get(0).getId());
        assertEquals("PAT-1", resultado.get(0).getAssetCode());
    }

    @Test
    void findRentalUnitsShouldThrowExceptionWhenRentalDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findRentalUnits(1L));
    }

    @Test
    void findHistoryShouldReturnStatusHistory() {
        RentalStatusHistory history = new RentalStatusHistory();
        history.setId(10L);
        RentalStatusHistoryDTO historyDTO = new RentalStatusHistoryDTO();
        historyDTO.setId(10L);
        historyDTO.setNewStatus("RENTED");
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(historyRepository.findByRentalIdOrderByChangedAtAsc(1L)).thenReturn(Collections.singletonList(history));
        when(mapper.toDTO(history)).thenReturn(historyDTO);

        List<RentalStatusHistoryDTO> resultado = service.findHistory(1L);

        assertEquals(1, resultado.size());
        assertEquals("RENTED", resultado.get(0).getNewStatus());
    }

    @Test
    void findHistoryShouldThrowExceptionWhenRentalDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findHistory(1L));
    }

    @Test
    void receiptShouldReturnDocument() {
        byte[] document = new byte[]{1, 2, 3};
        when(documentService.receipt(1L)).thenReturn(document);

        byte[] resultado = service.receipt(1L);

        assertArrayEquals(document, resultado);
        verify(documentService).receipt(1L);
    }

    @Test
    void receiptShouldThrowExceptionWhenRentalDoesNotExist() {
        when(documentService.receipt(1L)).thenThrow(new ResourceNotFoundException("Locação não encontrada"));

        assertThrows(ResourceNotFoundException.class, () -> service.receipt(1L));
    }

    @Test
    void fiscalCouponShouldReturnDocument() {
        byte[] document = new byte[]{4, 5, 6};
        when(documentService.fiscalCoupon(1L)).thenReturn(document);

        byte[] resultado = service.fiscalCoupon(1L);

        assertArrayEquals(document, resultado);
        verify(documentService).fiscalCoupon(1L);
    }

    @Test
    void fiscalCouponShouldThrowExceptionWhenRentalDoesNotExist() {
        when(documentService.fiscalCoupon(1L)).thenThrow(new ResourceNotFoundException("Locação não encontrada"));

        assertThrows(ResourceNotFoundException.class, () -> service.fiscalCoupon(1L));
    }

    @Test
    void deleteShouldDeleteRentalAndReleaseUnits() {
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(itemRepository.findByRentalIdOrderById(1L)).thenReturn(Collections.singletonList(rentalItem));
        when(rentalItemUnitRepository.findByRentalItemRentalIdOrderById(1L))
                .thenReturn(Collections.singletonList(rentalItemUnit));

        service.delete(1L);

        verify(stockService).releaseUnitsForDeletion(rental, Collections.singletonList(rentalItem),
                Collections.singletonList(rentalItemUnit));
        verify(rentalItemUnitRepository).deleteByRentalItemRentalId(1L);
        verify(itemRepository).deleteByRentalId(1L);
        verify(historyRepository).deleteByRentalId(1L);
        verify(repository).delete(rental);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenRentalDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));

        verify(repository, never()).delete(any());
        verify(stockService, never()).releaseUnitsForDeletion(any(), any(), any());
    }
}
