package com.uapa.factory;

import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceFactoryTest {

    @Test
    void testCreateDummyInvoice() {
        // When
        Invoice invoice = InvoiceFactory.createDummyInvoice();

        // Then
        assertNotNull(invoice.getId(), "Invoice ID should not be null");
        assertEquals(LocalDate.now(), invoice.getDate(),
                "Invoice date should be today's date");
        assertNotNull(invoice.getItems(), "Items list should not be null");
        assertFalse(invoice.getItems().isEmpty(), "Items list should not be empty");

        // Check total against sum of item price * quantity
        double expectedTotal = invoice.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        assertEquals(expectedTotal, invoice.getTotal(), 0.0001,
                "Invoice total should match the sum of item prices * quantity");
    }

    @Test
    void testCreateInvoice_withItems() {
        // Given
        InvoiceItem item1 = InvoiceItem.builder()
                .product("Test Product 1")
                .quantity(2)
                .price(10.0)
                .build();
        InvoiceItem item2 = InvoiceItem.builder()
                .product("Test Product 2")
                .quantity(3)
                .price(5.0)
                .build();
        List<InvoiceItem> items = List.of(item1, item2);

        // When
        Invoice invoice = InvoiceFactory.createInvoice(items);

        // Then
        assertNotNull(invoice.getId(), "Invoice ID should not be null");
        assertEquals(LocalDate.now(), invoice.getDate(),
                "Invoice date should be today's date");
        assertEquals(2, invoice.getItems().size(),
                "Should create an invoice with 2 items");

        // Check total
        double expectedTotal = (2 * 10.0) + (3 * 5.0);
        assertEquals(expectedTotal, invoice.getTotal(), 0.0001,
                "Invoice total should match the sum of item prices * quantity");
    }

    @Test
    void testCreateInvoice_withEmptyItems() {
        // Given
        List<InvoiceItem> emptyItems = List.of();

        // When
        Invoice invoice = InvoiceFactory.createInvoice(emptyItems);

        // Then
        assertNotNull(invoice.getId(), "Invoice ID should not be null");
        assertEquals(LocalDate.now(), invoice.getDate(),
                "Invoice date should be today's date");
        assertTrue(invoice.getItems().isEmpty(),
                "Items list should be empty");
        assertEquals(0.0, invoice.getTotal(), 0.0001,
                "Invoice total should be 0.0 for empty items list");
    }
}
