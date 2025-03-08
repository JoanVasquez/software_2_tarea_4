package com.uapa.event;

import com.uapa.model.Invoice;
import com.uapa.observer.InvoiceObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class SimpleInvoiceEventPublisherTest {

    private SimpleInvoiceEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SimpleInvoiceEventPublisher();
    }

    @Test
    void testRegisterObserver_andPublishInvoiceCreated() {
        // Given
        InvoiceObserver observerMock = mock(InvoiceObserver.class);
        publisher.registerObserver(observerMock);

        Invoice testInvoice = Invoice.builder()
                .id("INV-1001")
                .date(LocalDate.now())
                .total(123.45)
                .build();

        // When
        publisher.publishInvoiceCreated(testInvoice);

        // Then
        verify(observerMock, times(1)).update(testInvoice);
    }

    @Test
    void testMultipleObservers_allNotified() {
        // Given
        InvoiceObserver observerMock1 = mock(InvoiceObserver.class);
        InvoiceObserver observerMock2 = mock(InvoiceObserver.class);
        publisher.registerObserver(observerMock1);
        publisher.registerObserver(observerMock2);

        Invoice testInvoice = Invoice.builder()
                .id("INV-2002")
                .date(LocalDate.now())
                .total(200.0)
                .build();

        // When
        publisher.publishInvoiceCreated(testInvoice);

        // Then
        // Both observers should receive an update
        verify(observerMock1, times(1)).update(testInvoice);
        verify(observerMock2, times(1)).update(testInvoice);
    }

    @Test
    void testUnregisterObserver_notNotified() {
        // Given
        InvoiceObserver observerMock = mock(InvoiceObserver.class);
        publisher.registerObserver(observerMock);
        publisher.unregisterObserver(observerMock);

        Invoice testInvoice = Invoice.builder()
                .id("INV-3003")
                .date(LocalDate.now())
                .total(300.0)
                .build();

        // When
        publisher.publishInvoiceCreated(testInvoice);

        // Then
        // Since the observer has been unregistered, it should not receive an update
        verify(observerMock, never()).update(any(Invoice.class));
    }

    @Test
    void testNoObservers_noExceptions() {
        // Given
        Invoice testInvoice = Invoice.builder()
                .id("INV-4004")
                .date(LocalDate.now())
                .total(400.0)
                .build();

        // When / Then
        // Should cause no errors, even though no observers are registered
        publisher.publishInvoiceCreated(testInvoice);
    }
}
