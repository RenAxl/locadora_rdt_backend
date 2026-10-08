package com.locadora_rdt_backend.modules.rentals.rental.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalItemRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalRepository;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalDocumentServiceTests {

    @Mock
    private RentalRepository repository;

    @Mock
    private RentalItemRepository itemRepository;

    @InjectMocks
    private RentalDocumentService service;

    private Rental rental;
    private RentalItem rentalItem;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("Joao");
        customer.setCpf("12345678900");

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(3L);
        paymentMethod.setName("Pix");

        rental = new Rental();
        rental.setId(1L);
        rental.setRentalNumber("LOC-1");
        rental.setCustomer(customer);
        rental.setPaymentMethod(paymentMethod);
        rental.setStatus(RentalConstants.STATUS_DELIVERED);
        rental.setTotalAmount(new BigDecimal("20.00"));
        rental.setDiscount(new BigDecimal("1.00"));
        rental.setRentalStartDate(Instant.parse("2026-01-01T12:00:00Z"));
        rental.setEffectiveReturnDate(Instant.parse("2026-01-03T12:00:00Z"));
        rental.setUpdatedBy("Usuário Teste");

        Item item = new Item();
        item.setId(4L);
        item.setName("Furadeira");

        rentalItem = new RentalItem();
        rentalItem.setRental(rental);
        rentalItem.setItem(item);
        rentalItem.setQuantity(1);
        rentalItem.setUnitPrice(new BigDecimal("10.00"));
        rentalItem.setSubtotal(new BigDecimal("20.00"));
    }

    @Test
    void receiptShouldReturnReceiptPdf() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(itemRepository.findByRentalIdOrderById(1L)).thenReturn(Collections.singletonList(rentalItem));

        byte[] resultado = service.receipt(1L);

        assertNotNull(resultado);
        PdfReader reader = new PdfReader(resultado);
        assertEquals(1, reader.getNumberOfPages());
        reader.close();
        verify(itemRepository).findByRentalIdOrderById(1L);
    }

    @Test
    void receiptShouldThrowExceptionWhenRentalIsNotDelivered() {
        rental.setStatus(RentalConstants.STATUS_RENTED);
        when(repository.findById(1L)).thenReturn(Optional.of(rental));

        assertThrows(IllegalArgumentException.class, () -> service.receipt(1L));

        verify(itemRepository, never()).findByRentalIdOrderById(1L);
    }

    @Test
    void fiscalCouponShouldReturnFiscalCouponPdf() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(rental));
        when(itemRepository.findByRentalIdOrderById(1L)).thenReturn(Collections.singletonList(rentalItem));

        byte[] resultado = service.fiscalCoupon(1L);

        assertNotNull(resultado);
        PdfReader reader = new PdfReader(resultado);
        assertEquals(1, reader.getNumberOfPages());
        reader.close();
        verify(itemRepository).findByRentalIdOrderById(1L);
    }

    @Test
    void fiscalCouponShouldThrowExceptionWhenRentalDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.fiscalCoupon(1L));

        verify(itemRepository, never()).findByRentalIdOrderById(1L);
    }
}
