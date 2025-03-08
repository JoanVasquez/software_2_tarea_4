package com.uapa.observer;

import com.uapa.model.Invoice;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceGUIObserverTest {

    @Test
    void testUpdate_appendsTextToTextArea() throws Exception {
        // Create a JTextArea to simulate the GUI component
        JTextArea textArea = new JTextArea();

        // Instantiate the observer, passing the textArea
        InvoiceGUIObserver observer = new InvoiceGUIObserver(textArea);

        // Create a sample Invoice
        Invoice invoice = Invoice.builder()
                .id("INV-001")
                .date(LocalDate.of(2025, 3, 10))
                .total(99.99)
                .build();

        // Trigger the observer's update
        observer.update(invoice);

        /*
         * Because update() uses SwingUtilities.invokeLater(...),
         * we want to ensure that the runnable on the EDT finishes
         * before we check the text area content.
         *
         * invokeAndWait(...) blocks until all pending EDT events have been processed.
         */
        SwingUtilities.invokeAndWait(() -> {
            /* No-op, just wait for EDT */ });

        // Now check that our text area has the expected text
        String logContent = textArea.getText();
        assertTrue(logContent.contains("Factura generada: INV-001"),
                "Log should contain the invoice ID");
        assertTrue(logContent.contains("Total: 99.99"),
                "Log should contain the invoice total");
    }
}
