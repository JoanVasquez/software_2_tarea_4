package com.uapa.repository;

import com.uapa.config.DatabaseConnector;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InvoiceRepositoryImplTest {

    private static final String JDBC_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "";
    private static final String PASSWORD = "";

    private DatabaseConnector connector;
    private InvoiceRepositoryImpl repository;

    @BeforeEach
    void setUp() throws SQLException {
        // Create an anonymous DatabaseConnector that returns an H2 connection
        connector = new DatabaseConnector() {
            @Override
            public Connection getConnection() {
                try {
                    return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Initialize our repository with the in-memory connector
        repository = new InvoiceRepositoryImpl(connector);

        // Create tables for testing
        try (Connection connection = connector.getConnection();
                Statement statement = connection.createStatement()) {

            // Create invoices table
            statement.execute("CREATE TABLE IF NOT EXISTS invoices (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "date DATE NOT NULL, " +
                    "total DOUBLE NOT NULL)");

            // Create invoice_items table
            statement.execute("CREATE TABLE IF NOT EXISTS invoice_items (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "invoice_id VARCHAR(50) NOT NULL, " +
                    "product VARCHAR(100) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DOUBLE NOT NULL)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up tables so each test is independent
        try (Connection connection = connector.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS invoice_items");
            statement.execute("DROP TABLE IF EXISTS invoices");
        }
    }

    @Test
    void testSaveInvoice_withoutItems() {
        // Prepare data
        Invoice invoice = Invoice.builder()
                .id("INV-0001")
                .date(LocalDate.of(2025, 1, 10))
                .total(250.0)
                .build();

        // Execute method
        repository.saveInvoice(invoice);

        // Verify with getInvoiceById
        Optional<Invoice> foundInvoice = repository.getInvoiceById("INV-0001");
        assertTrue(foundInvoice.isPresent(), "Invoice should be present");
        assertEquals("INV-0001", foundInvoice.get().getId());
        assertEquals(LocalDate.of(2025, 1, 10), foundInvoice.get().getDate());
        assertEquals(250.0, foundInvoice.get().getTotal());
    }

    @Test
    void testSaveInvoice_withItems() {
        // Prepare data
        InvoiceItem item1 = InvoiceItem.builder()
                .product("Pen")
                .quantity(10)
                .price(1.50)
                .build();

        InvoiceItem item2 = InvoiceItem.builder()
                .product("Notebook")
                .quantity(5)
                .price(3.75)
                .build();

        Invoice invoice = Invoice.builder()
                .id("INV-0002")
                .date(LocalDate.of(2025, 3, 8))
                .total(30.0)
                .items(List.of(item1, item2))
                .build();

        // Execute method
        repository.saveInvoice(invoice);

        // Verify invoice with getInvoiceById
        Optional<Invoice> foundInvoiceOpt = repository.getInvoiceById("INV-0002");
        assertTrue(foundInvoiceOpt.isPresent(), "Invoice INV-0002 should be present");
        Invoice foundInvoice = foundInvoiceOpt.get();
        assertEquals("INV-0002", foundInvoice.getId());
        assertEquals(LocalDate.of(2025, 3, 8), foundInvoice.getDate());
        assertEquals(30.0, foundInvoice.getTotal());

        // Verify invoice items via getAllInvoices
        List<Invoice> allInvoices = repository.getAllInvoices();
        assertEquals(1, allInvoices.size(), "There should be exactly 1 invoice stored");

        Invoice retrievedInvoice = allInvoices.get(0);
        assertNotNull(retrievedInvoice.getItems());
        assertEquals(2, retrievedInvoice.getItems().size(), "Invoice should have 2 items");

        // Check the items
        InvoiceItem firstItem = retrievedInvoice.getItems().get(0);
        assertEquals("Pen", firstItem.getProduct());
        assertEquals(10, firstItem.getQuantity());
        assertEquals(1.50, firstItem.getPrice());

        InvoiceItem secondItem = retrievedInvoice.getItems().get(1);
        assertEquals("Notebook", secondItem.getProduct());
        assertEquals(5, secondItem.getQuantity());
        assertEquals(3.75, secondItem.getPrice());
    }

    @Test
    void testGetInvoiceById_notFound() {
        Optional<Invoice> result = repository.getInvoiceById("NON_EXISTENT");
        assertFalse(result.isPresent(), "No invoice should be found for NON_EXISTENT");
    }

    @Test
    void testGetAllInvoices_multipleInvoices() {
        // Create first invoice
        Invoice invoice1 = Invoice.builder()
                .id("INV-1000")
                .date(LocalDate.of(2025, 1, 1))
                .total(100.0)
                .build();
        repository.saveInvoice(invoice1);

        // Create second invoice
        InvoiceItem item = InvoiceItem.builder()
                .product("Markers")
                .quantity(4)
                .price(2.5)
                .build();
        Invoice invoice2 = Invoice.builder()
                .id("INV-2000")
                .date(LocalDate.of(2025, 2, 1))
                .total(10.0)
                .items(List.of(item))
                .build();
        repository.saveInvoice(invoice2);

        // Retrieve
        List<Invoice> invoices = repository.getAllInvoices();
        assertEquals(2, invoices.size(), "Should return exactly 2 invoices");

        // Quick check for IDs
        assertTrue(invoices.stream().anyMatch(inv -> "INV-1000".equals(inv.getId())));
        assertTrue(invoices.stream().anyMatch(inv -> "INV-2000".equals(inv.getId())));
    }
}
