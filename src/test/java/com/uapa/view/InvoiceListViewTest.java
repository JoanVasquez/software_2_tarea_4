package com.uapa.view;

import com.uapa.controller.InvoiceController;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import org.junit.jupiter.api.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component; // <-- ADD THIS IMPORT
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceListViewTest {

    private InvoiceController mockController;

    @BeforeEach
    void setUp() {
        mockController = mock(InvoiceController.class);
    }

    @Test
    void testInitialLoadInvoices_noItems() throws Exception {
        // Given: A single invoice with no items
        Invoice invoice = Invoice.builder()
                .id("INV-0001")
                .date(LocalDate.of(2025, 3, 10))
                .total(99.99)
                .build();
        when(mockController.getAllInvoices()).thenReturn(Collections.singletonList(invoice));

        // When/Then: Construct the view on the Event Dispatch Thread
        SwingUtilities.invokeAndWait(() -> {
            InvoiceListView view = new InvoiceListView(mockController);

            view.setVisible(true);

            // Check the frame properties
            assertEquals("Listado de Facturas", view.getTitle());
            assertTrue(view.isVisible()); // By default, JFrame is visible after pack/show,
                                          // but it may differ depending on your constructor logic.

            // Retrieve the table model to check row data
            JTable table = getTableFromView(view);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            assertEquals(1, model.getRowCount(), "Expect exactly 1 row for one invoice without items");

            // Verify row content
            Object invoiceId = model.getValueAt(0, 0);
            Object date = model.getValueAt(0, 1);
            Object total = model.getValueAt(0, 2);
            Object product = model.getValueAt(0, 3);
            Object quantity = model.getValueAt(0, 4);
            Object price = model.getValueAt(0, 5);

            assertEquals("INV-0001", invoiceId);
            assertEquals("2025-03-10", date);
            // For an invoice with no items, the code adds a row with empty
            // product/quantity/price.
            // Also note the code uses "invoice.getTotal()" for the row if no items exist.
            // So the total should be 99.99 from the invoice object itself.
            assertEquals(99.99, (double) total, 0.0001);
            assertEquals("", product);
            assertEquals("", quantity);
            assertEquals("", price);

            // Clean up (dispose the window)
            view.dispose();
        });
    }

    @Test
    void testInitialLoadInvoices_withItems() throws Exception {
        // Given: An invoice with multiple items
        InvoiceItem item1 = InvoiceItem.builder()
                .product("Test Product A")
                .quantity(2)
                .price(10.0)
                .build();
        InvoiceItem item2 = InvoiceItem.builder()
                .product("Test Product B")
                .quantity(1)
                .price(15.0)
                .build();

        Invoice invoice = Invoice.builder()
                .id("INV-0002")
                .date(LocalDate.of(2025, 4, 1))
                .items(Arrays.asList(item1, item2))
                // Note that the code calculates row total as item.getPrice() *
                // item.getQuantity()
                // for each row in the table. The "invoice.getTotal()" is not used if items
                // exist
                // (the code uses Double.parseDouble(String.valueOf(item.getPrice())) *
                // quantity).
                // But let's keep a total for logical completeness.
                .total(item1.getPrice() * item1.getQuantity() + item2.getPrice() * item2.getQuantity())
                .build();
        when(mockController.getAllInvoices()).thenReturn(Collections.singletonList(invoice));

        // Construct and verify in the EDT
        SwingUtilities.invokeAndWait(() -> {
            InvoiceListView view = new InvoiceListView(mockController);
            JTable table = getTableFromView(view);
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // For an invoice with 2 items, we should have 2 rows
            assertEquals(2, model.getRowCount());

            // Check row #1
            Object invoiceId1 = model.getValueAt(0, 0);
            Object date1 = model.getValueAt(0, 1);
            double total1 = (double) model.getValueAt(0, 2); // item1 price * quantity
            Object product1 = model.getValueAt(0, 3);
            int quantity1 = (int) model.getValueAt(0, 4);
            double price1 = (double) model.getValueAt(0, 5);

            assertEquals("INV-0002", invoiceId1);
            assertEquals("2025-04-01", date1);
            assertEquals(20.0, total1, 0.0001); // 2 * 10.0
            assertEquals("Test Product A", product1);
            assertEquals(2, quantity1);
            assertEquals(10.0, price1, 0.0001);

            // Check row #2
            Object invoiceId2 = model.getValueAt(1, 0);
            Object date2 = model.getValueAt(1, 1);
            double total2 = (double) model.getValueAt(1, 2);
            Object product2 = model.getValueAt(1, 3);
            int quantity2 = (int) model.getValueAt(1, 4);
            double price2 = (double) model.getValueAt(1, 5);

            assertEquals("INV-0002", invoiceId2);
            assertEquals("2025-04-01", date2);
            assertEquals(15.0, total2, 0.0001); // 1 * 15.0
            assertEquals("Test Product B", product2);
            assertEquals(1, quantity2);
            assertEquals(15.0, price2, 0.0001);

            // Clean up
            view.dispose();
        });
    }

    @Test
    void testRefreshButton() throws Exception {
        // Given: The controller returns an initial list with 1 invoice
        Invoice invoice1 = Invoice.builder()
                .id("INV-AAA")
                .date(LocalDate.now())
                .total(50.0)
                .build();

        // Then we want a second invocation to return a different list (2 invoices, say)
        Invoice invoice2 = Invoice.builder()
                .id("INV-BBB")
                .date(LocalDate.now())
                .total(100.0)
                .build();
        Invoice invoice3 = Invoice.builder()
                .id("INV-CCC")
                .date(LocalDate.now())
                .total(200.0)
                .build();

        // Setup stubbing for consecutive calls
        when(mockController.getAllInvoices())
                .thenReturn(Collections.singletonList(invoice1))
                .thenReturn(Arrays.asList(invoice2, invoice3));

        // Construct the view
        SwingUtilities.invokeAndWait(() -> {
            InvoiceListView view = new InvoiceListView(mockController);
            JTable table = getTableFromView(view);
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Initially, should have 1 row
            assertEquals(1, model.getRowCount(), "Initially should display 1 invoice");

            // Press the "Refrescar" button, which calls loadInvoices() again
            JButton refreshButton = getRefreshButtonFromView(view);
            refreshButton.doClick();

            // Now the mock returns 2 invoices, so the table should be updated
            assertEquals(2, model.getRowCount(), "After refresh, should display 2 invoices");
            // Verify the second row belongs to the second invoice
            Object secondInvoiceId = model.getValueAt(1, 0);
            assertEquals("INV-CCC", secondInvoiceId);

            // Clean up
            view.dispose();
        });
    }

    // Helper method to extract the table from the frame
    private JTable getTableFromView(InvoiceListView view) {
        // Because we know the InvoiceListView layout,
        // we can search its children to find the table.
        // Alternatively, we might expose "getTable()" in the production code, but
        // here's a direct approach:
        for (Component comp : view.getContentPane().getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                JViewport viewport = scrollPane.getViewport();
                Component viewComp = viewport.getView();
                if (viewComp instanceof JTable) {
                    return (JTable) viewComp;
                }
            }
        }
        throw new IllegalStateException("JTable not found in InvoiceListView");
    }

    // Helper method to get the "Refrescar" button from the view
    private JButton getRefreshButtonFromView(InvoiceListView view) {
        for (Component comp : view.getContentPane().getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if ("Refrescar".equals(button.getText())) {
                    return button;
                }
            }
        }
        throw new IllegalStateException("Refrescar button not found in InvoiceListView");
    }
}
