package com.uapa.integration;

import com.uapa.config.DatabaseConnector;
import com.uapa.config.H2DatabaseConnector;
import com.uapa.controller.InvoiceController;
import com.uapa.event.InvoiceEventPublisher;
import com.uapa.event.SimpleInvoiceEventPublisher;
import com.uapa.factory.InvoiceFactory;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import com.uapa.repository.InvoiceRepositoryImpl;
import com.uapa.service.InvoiceService;
import com.uapa.service.InvoiceServiceImpl;
import com.uapa.view.InvoiceListView;
import com.uapa.view.InvoiceRealView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceViewsIntegrationTests {

    // Force headless mode off so that GUI windows are rendered (if environment
    // supports it)
    static {
        System.setProperty("java.awt.headless", "false");
    }

    private DatabaseConnector connector;
    private InvoiceRepositoryImpl repository;
    private InvoiceEventPublisher eventPublisher;
    private InvoiceService service;
    private InvoiceController controller;

    @BeforeEach
    void setupIntegration() throws SQLException {
        // Initialize the in-memory H2 database and necessary components.
        connector = new H2DatabaseConnector();
        repository = new InvoiceRepositoryImpl(connector);
        eventPublisher = new SimpleInvoiceEventPublisher();
        service = new InvoiceServiceImpl(repository, eventPublisher);
        controller = new InvoiceController(service);

        // Ensure required tables exist.
        try (Connection connection = connector.getConnection();
                Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS invoices (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "date DATE NOT NULL, " +
                    "total DOUBLE NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS invoice_items (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "invoice_id VARCHAR(50) NOT NULL, " +
                    "product VARCHAR(100) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DOUBLE NOT NULL)");
        }
    }

    @AfterEach
    void tearDownIntegration() throws SQLException {
        // Clean up tables so tests remain independent.
        try (Connection connection = connector.getConnection();
                Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS invoice_items");
            stmt.execute("DROP TABLE IF EXISTS invoices");
        }
    }

    /**
     * Helper method to access private fields via reflection.
     */
    private <T> T getPrivateField(Object instance, String fieldName, Class<T> fieldType) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return fieldType.cast(field.get(instance));
    }

    @Test
    void testInvoiceRealView_CreateInvoiceIntegration() throws Exception {
        // Run InvoiceRealView on the EDT.
        SwingUtilities.invokeAndWait(() -> {
            try {
                InvoiceRealView view = new InvoiceRealView(controller);
                view.setVisible(true);

                // Access private fields.
                JTextField productField = getPrivateField(view, "productField", JTextField.class);
                JTextField quantityField = getPrivateField(view, "quantityField", JTextField.class);
                JTextField priceField = getPrivateField(view, "priceField", JTextField.class);
                JButton addItemButton = getPrivateField(view, "addItemButton", JButton.class);
                JButton createInvoiceButton = getPrivateField(view, "createInvoiceButton", JButton.class);
                JTextArea itemsArea = getPrivateField(view, "itemsArea", JTextArea.class);

                // Simulate user input.
                productField.setText("Integration Product");
                quantityField.setText("2");
                priceField.setText("25.0");

                // Click "Agregar Producto" button.
                addItemButton.doClick();
                assertTrue(itemsArea.getText().contains("Integration Product"),
                        "Items area should contain the product name");

                // Click "Crear Factura" button.
                createInvoiceButton.doClick();
                assertEquals("", itemsArea.getText(),
                        "Items area should be cleared after invoice creation");

                // Wait a moment so the view is visible.
                Thread.sleep(2000);
                view.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for asynchronous processing.
        Thread.sleep(1500);

        // Verify that an invoice was saved.
        List<Invoice> invoices = repository.getAllInvoices();
        assertFalse(invoices.isEmpty(), "Repository should contain at least one invoice");
        Invoice createdInvoice = invoices.get(0);
        // Expected total = 2 * 25.0 = 50.0
        assertEquals(50.0, createdInvoice.getTotal(), 0.0001, "Invoice total should match expected value");
    }

}
