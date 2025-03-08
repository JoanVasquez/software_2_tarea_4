package com.uapa.service;

import com.uapa.event.InvoiceEventPublisher;
import com.uapa.model.Invoice;
import com.uapa.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceImplTest {

    private InvoiceRepository repositoryMock;
    private InvoiceEventPublisher eventPublisherMock;
    private InvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        // Create mocks
        repositoryMock = mock(InvoiceRepository.class);
        eventPublisherMock = mock(InvoiceEventPublisher.class);

        // Instantiate the service with mocks
        service = new InvoiceServiceImpl(repositoryMock, eventPublisherMock);
    }

    @Test
    void testCreateInvoice() throws InterruptedException {
        // Given
        Invoice invoice = Invoice.builder()
                .id("INV-001")
                .date(LocalDate.of(2025, 3, 10))
                .total(100.0)
                .build();

        // When
        service.createInvoice(invoice);

        // Because the method is async, we wait a short time for the background thread
        Thread.sleep(1200);

        // Then - verify that repository.saveInvoice was called
        verify(repositoryMock, times(1)).saveInvoice(invoice);

        // Then - verify that eventPublisher.publishInvoiceCreated was called
        verify(eventPublisherMock, times(1)).publishInvoiceCreated(invoice);
    }

    @Test
    void testGetAllInvoices() {
        // Given
        Invoice invoice = Invoice.builder()
                .id("INV-002")
                .date(LocalDate.of(2025, 4, 1))
                .total(50.0)
                .build();
        List<Invoice> mockInvoices = Collections.singletonList(invoice);

        // Configure mock to return our mockInvoices
        when(repositoryMock.getAllInvoices()).thenReturn(mockInvoices);

        // When
        List<Invoice> result = service.getAllInvoices();

        // Then
        assertEquals(1, result.size(), "Should return exactly one invoice");
        assertEquals("INV-002", result.get(0).getId(), "Invoice ID should match");
        assertEquals(50.0, result.get(0).getTotal(), "Invoice total should match");

        // Also verify that the repository was indeed called
        verify(repositoryMock, times(1)).getAllInvoices();
    }

    @Test
    void testCreateInvoice_withArgumentCaptor() throws InterruptedException {
        // This is an optional example showing how to capture the actual invoice
        // argument passed to the repository and event publisher.
        Invoice invoice = Invoice.builder()
                .id("INV-003")
                .date(LocalDate.now())
                .total(200.0)
                .build();

        service.createInvoice(invoice);
        Thread.sleep(1200); // Wait for the asynchronous call

        // Capture the arguments passed to the repository
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(repositoryMock, times(1)).saveInvoice(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertNotNull(capturedInvoice);
        assertEquals("INV-003", capturedInvoice.getId());

        // Capture the arguments passed to the event publisher
        ArgumentCaptor<Invoice> eventCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(eventPublisherMock, times(1)).publishInvoiceCreated(eventCaptor.capture());
        Invoice publishedInvoice = eventCaptor.getValue();
        assertEquals("INV-003", publishedInvoice.getId());
    }

}
