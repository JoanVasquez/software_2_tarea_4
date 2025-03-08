package com.uapa.integration;

import com.uapa.config.DatabaseConnector;
import com.uapa.config.H2DatabaseConnector;
import com.uapa.controller.InvoiceController;
import com.uapa.event.InvoiceEventPublisher;
import com.uapa.event.SimpleInvoiceEventPublisher;
import com.uapa.model.Invoice;
import com.uapa.repository.InvoiceRepositoryImpl;
import com.uapa.service.InvoiceService;
import com.uapa.service.InvoiceServiceImpl;
import com.uapa.view.InvoiceListView;
import com.uapa.view.InvoiceRealView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EndToEndTest {

    private DatabaseConnector connector;
    private InvoiceRepositoryImpl repository;
    private InvoiceEventPublisher eventPublisher;
    private InvoiceService service;
    private InvoiceController controller;

    @BeforeEach
    public void setup() throws Exception {
        // Initialize the H2 in-memory database connector for testing
        connector = new H2DatabaseConnector();
        // Create necessary tables
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
        repository = new InvoiceRepositoryImpl(connector);
        eventPublisher = new SimpleInvoiceEventPublisher();
        service = new InvoiceServiceImpl(repository, eventPublisher);
        controller = new InvoiceController(service);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Drop tables so each test is independent.
        try (Connection connection = connector.getConnection();
                Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS invoice_items");
            stmt.execute("DROP TABLE IF EXISTS invoices");
        }
    }

    @Test
    public void testEndToEndInvoiceCreationAndListView() throws Exception {
        // Step 1: Use InvoiceRealView to create an invoice.
        SwingUtilities.invokeAndWait(() -> {
            try {
                InvoiceRealView realView = new InvoiceRealView(controller);
                realView.setVisible(true);

                // Access private fields via reflection.
                JTextField productField = getPrivateField(realView, "productField", JTextField.class);
                JTextField quantityField = getPrivateField(realView, "quantityField", JTextField.class);
                JTextField priceField = getPrivateField(realView, "priceField", JTextField.class);
                JButton addItemButton = getPrivateField(realView, "addItemButton", JButton.class);
                JButton createInvoiceButton = getPrivateField(realView, "createInvoiceButton", JButton.class);
                JTextArea itemsArea = getPrivateField(realView, "itemsArea", JTextArea.class);

                // Simulate user input for one invoice item.
                productField.setText("EndToEnd Product");
                quantityField.setText("3");
                priceField.setText("10.0");

                // Simulate adding the product.
                addItemButton.doClick();
                assertTrue(itemsArea.getText().contains("EndToEnd Product"),
                        "Items area should display the added product");

                // Simulate creating the invoice.
                createInvoiceButton.doClick();

                // Wait briefly for asynchronous processing.
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                realView.dispose();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Allow time for the asynchronous invoice creation to complete.
        Thread.sleep(1500);

        // Verify via repository that the invoice has been saved.
        List<Invoice> invoices = repository.getAllInvoices();
        assertFalse(invoices.isEmpty(), "At least one invoice should be present in the repository");
        Invoice createdInvoice = invoices.get(0);
        // Expected total: 3 * 10.0 = 30.0
        assertEquals(30.0, createdInvoice.getTotal(), 0.0001, "Invoice total should be calculated correctly");

        // Step 2: Use InvoiceListView to display the stored invoice.
        SwingUtilities.invokeAndWait(() -> {
            try {
                InvoiceListView listView = new InvoiceListView(controller);
                listView.setVisible(true);

                // Access the private DefaultTableModel via reflection.
                DefaultTableModel tableModel = getPrivateField(listView, "tableModel", DefaultTableModel.class);

                // Check that at least one row is present.
                assertTrue(tableModel.getRowCount() > 0, "Invoice list should contain at least one row");

                // For invoices with items, each item is displayed in its own row.
                // Since we added one item, verify that the product, quantity, price, and
                // computed total match.
                Object productCell = tableModel.getValueAt(0, 3);
                Object quantityCell = tableModel.getValueAt(0, 4);
                Object priceCell = tableModel.getValueAt(0, 5);
                Object totalCell = tableModel.getValueAt(0, 2);

                assertEquals("EndToEnd Product", productCell, "Invoice product should be 'EndToEnd Product'");
                assertEquals(3, ((Number) quantityCell).intValue(), "Invoice quantity should be 3");
                assertEquals(10.0, ((Number) priceCell).doubleValue(), 0.0001, "Invoice price should be 10.0");
                assertEquals(30.0, ((Number) totalCell).doubleValue(), 0.0001, "Invoice total should be 30.0");

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                listView.dispose();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    // Helper method to access private fields using reflection.
    private <T> T getPrivateField(Object instance, String fieldName, Class<T> type) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(instance));
    }
}
