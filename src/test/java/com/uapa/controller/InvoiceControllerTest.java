package com.uapa.controller;

import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import com.uapa.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceControllerTest {

    private InvoiceService mockService;
    private InvoiceController controller;

    @BeforeEach
    void setUp() {
        mockService = mock(InvoiceService.class);
        controller = new InvoiceController(mockService);
    }

    @Test
    void testGenerateInvoice() {
        // When
        controller.generateInvoice();

        // Then
        // We only know that InvoiceFactory.createDummyInvoice() is used internally,
        // so let's verify that createInvoice(...) on the service is called once with
        // any Invoice.
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(mockService, times(1)).createInvoice(invoiceCaptor.capture());

        Invoice captured = invoiceCaptor.getValue();
        assertNotNull(captured, "Invoice passed to the service should not be null");
        // We can do a minimal check that items are not empty if the factory is supposed
        // to create items
        // (This depends on how your factory is implemented)
        assertFalse(captured.getItems().isEmpty(), "Dummy invoice should have items");
        assertTrue(captured.getTotal() > 0, "Invoice total should be greater than 0");
    }

    @Test
    void testGenerateRealInvoice() {
        // Given
        InvoiceItem item = InvoiceItem.builder()
                .product("Test Product")
                .quantity(2)
                .price(10.0)
                .build();
        List<InvoiceItem> items = List.of(item);

        // When
        controller.generateRealInvoice(items);

        // Then
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(mockService, times(1)).createInvoice(invoiceCaptor.capture());

        Invoice captured = invoiceCaptor.getValue();
        assertNotNull(captured, "Invoice passed to the service should not be null");

        // We can verify the invoice contains the items we passed in
        assertEquals(1, captured.getItems().size(), "Invoice should have exactly one item");
        assertEquals("Test Product", captured.getItems().get(0).getProduct());
        assertEquals(2, captured.getItems().get(0).getQuantity());
        assertEquals(10.0, captured.getItems().get(0).getPrice());
        // Check the total matches
        assertEquals(20.0, captured.getTotal(), 0.0001);
    }

    @Test
    void testGetAllInvoices() {
        // Given
        Invoice invoice = Invoice.builder()
                .id("INV-1234")
                .date(LocalDate.now())
                .total(100.0)
                .build();
        List<Invoice> mockList = Collections.singletonList(invoice);

        // We tell the mock to return this list when getAllInvoices() is called
        when(mockService.getAllInvoices()).thenReturn(mockList);

        // When
        List<Invoice> result = controller.getAllInvoices();

        // Then
        // Check that we indeed get the mock list
        assertEquals(1, result.size(), "Should return one invoice");
        assertEquals("INV-1234", result.get(0).getId(), "Invoice ID should match");
        assertEquals(100.0, result.get(0).getTotal(), 0.0001, "Invoice total should match");

        // And verify the service was called
        verify(mockService, times(1)).getAllInvoices();
    }
}
