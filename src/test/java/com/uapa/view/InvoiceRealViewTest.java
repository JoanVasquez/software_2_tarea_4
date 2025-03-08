package com.uapa.view;

import com.uapa.controller.InvoiceController;
import com.uapa.model.InvoiceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.Component;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InvoiceRealViewTest {

    private InvoiceController mockController;
    private InvoiceRealView view;

    @BeforeEach
    void setUp() {
        // Create a mock InvoiceController and instantiate the view.
        mockController = mock(InvoiceController.class);
        view = new InvoiceRealView(mockController);
    }

    /**
     * Helper method to access private fields via reflection.
     */
    private <T> T getPrivateField(String fieldName, Class<T> fieldType) throws Exception {
        Field field = InvoiceRealView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return fieldType.cast(field.get(view));
    }

    @Test
    void testHandleAddItemValid() throws Exception {
        // Retrieve private Swing components.
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JTextArea itemsArea = getPrivateField("itemsArea", JTextArea.class);
        JTextField totalField = getPrivateField("totalField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);

        // Arrange: set valid input values.
        productField.setText("Test Product");
        quantityField.setText("3");
        priceField.setText("10.0");

        // Act: simulate clicking the "Agregar Producto" button.
        addItemButton.doClick();

        // Assert: item information should appear in the text area.
        String itemsText = itemsArea.getText();
        assertTrue(itemsText.contains("Test Product"));
        assertTrue(itemsText.contains("Cantidad: 3"));
        assertTrue(itemsText.contains("Precio: 10.00"));
        // Total: 3 * 10.0 = 30.0
        assertEquals("30.0", totalField.getText());

        // Verify that the input fields for product, quantity, and price have been
        // cleared.
        assertEquals("", productField.getText());
        assertEquals("", quantityField.getText());
        assertEquals("", priceField.getText());
    }

    @Test
    void testHandleAddItemEmptyProduct() throws Exception {
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JTextArea itemsArea = getPrivateField("itemsArea", JTextArea.class);

        // Arrange: empty product name.
        productField.setText("");
        quantityField.setText("2");
        priceField.setText("5.0");

        // Use static mocking to intercept JOptionPane calls.
        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            addItemButton.doClick();

            // Verify that the message dialog is shown with the proper error message.
            mockPane.verify(
                    () -> JOptionPane.showMessageDialog(any(Component.class), eq("Ingrese el nombre del producto.")),
                    times(1));
        }
        // Assert: no item was added.
        assertEquals("", itemsArea.getText());
    }

    @Test
    void testHandleAddItemInvalidQuantity() throws Exception {
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JTextArea itemsArea = getPrivateField("itemsArea", JTextArea.class);

        // Arrange: invalid quantity (non-numeric).
        productField.setText("Test Product");
        quantityField.setText("abc");
        priceField.setText("10.0");

        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            addItemButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Cantidad inválida.")),
                    times(1));
        }
        // Assert: itemsArea remains empty.
        assertEquals("", itemsArea.getText());
    }

    @Test
    void testHandleAddItemInvalidPrice() throws Exception {
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JTextArea itemsArea = getPrivateField("itemsArea", JTextArea.class);

        // Arrange: invalid price (non-numeric).
        productField.setText("Test Product");
        quantityField.setText("2");
        priceField.setText("xyz");

        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            addItemButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Precio inválido.")),
                    times(1));
        }
        // Assert: no item is added.
        assertEquals("", itemsArea.getText());
    }

    @Test
    void testHandleCreateInvoiceNoItems() throws Exception {
        JButton createInvoiceButton = getPrivateField("createInvoiceButton", JButton.class);

        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            createInvoiceButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Agregue al menos un ítem.")),
                    times(1));
        }
        // Verify that the controller is not invoked.
        verify(mockController, never()).generateRealInvoice(anyList());
    }

    @Test
    void testHandleCreateInvoiceInvalidDate() throws Exception {
        // Arrange: add one valid item.
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JTextField dateField = getPrivateField("dateField", JTextField.class);
        JButton createInvoiceButton = getPrivateField("createInvoiceButton", JButton.class);

        productField.setText("Test Product");
        quantityField.setText("2");
        priceField.setText("10.0");
        addItemButton.doClick();

        // Set an invalid date.
        dateField.setText("invalid-date");

        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            createInvoiceButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class),
                    eq("Fecha inválida. Use el formato yyyy-MM-dd.")), times(1));
        }
        // Controller should not be invoked.
        verify(mockController, never()).generateRealInvoice(anyList());
    }

    @Test
    void testHandleCreateInvoiceSuccess() throws Exception {
        // Arrange: add two items.
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JTextField dateField = getPrivateField("dateField", JTextField.class);
        JButton createInvoiceButton = getPrivateField("createInvoiceButton", JButton.class);
        JTextArea itemsArea = getPrivateField("itemsArea", JTextArea.class);

        productField.setText("Product A");
        quantityField.setText("2");
        priceField.setText("10.0");
        addItemButton.doClick();

        productField.setText("Product B");
        quantityField.setText("1");
        priceField.setText("5.0");
        addItemButton.doClick();

        // Use a valid date.
        dateField.setText(LocalDate.now().toString());

        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            createInvoiceButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Factura creada con éxito.")),
                    times(1));
        }
        // Verify that the controller's generateRealInvoice was invoked.
        verify(mockController, times(1)).generateRealInvoice(anyList());
        // Assert that the items area is cleared.
        assertEquals("", itemsArea.getText());
    }

    @Test
    void testHandleRemoveItemNoItems() throws Exception {
        JButton removeItemButton = getPrivateField("removeItemButton", JButton.class);

        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            removeItemButton.doClick();
            mockPane.verify(
                    () -> JOptionPane.showMessageDialog(any(Component.class), eq("No hay ítems para eliminar.")),
                    times(1));
        }
    }

    @Test
    void testHandleRemoveItemSuccess() throws Exception {
        // Arrange: add two items.
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JButton removeItemButton = getPrivateField("removeItemButton", JButton.class);
        JTextArea itemsArea = getPrivateField("itemsArea", JTextArea.class);

        productField.setText("Product A");
        quantityField.setText("1");
        priceField.setText("5.0");
        addItemButton.doClick();

        productField.setText("Product B");
        quantityField.setText("2");
        priceField.setText("10.0");
        addItemButton.doClick();

        // Use static mocking to simulate valid user input "0" for removal.
        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            mockPane.when(() -> JOptionPane.showInputDialog(any(Component.class), anyString()))
                    .thenReturn("0");

            removeItemButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Ítem eliminado con éxito.")),
                    times(1));
        }
        // Assert that the first item ("Product A") was removed.
        String itemsText = itemsArea.getText();
        assertFalse(itemsText.contains("Product A"));
        assertTrue(itemsText.contains("Product B"));
    }

    @Test
    void testHandleRemoveItemInvalidIndex() throws Exception {
        // Arrange: add one item.
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JButton removeItemButton = getPrivateField("removeItemButton", JButton.class);

        productField.setText("Product A");
        quantityField.setText("1");
        priceField.setText("5.0");
        addItemButton.doClick();

        // Simulate an out-of-range index ("5").
        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            mockPane.when(() -> JOptionPane.showInputDialog(any(Component.class), anyString()))
                    .thenReturn("5");

            removeItemButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Índice fuera de rango.")),
                    times(1));
        }
    }

    @Test
    void testHandleRemoveItemNonNumericIndex() throws Exception {
        // Arrange: add one item.
        JTextField productField = getPrivateField("productField", JTextField.class);
        JTextField quantityField = getPrivateField("quantityField", JTextField.class);
        JTextField priceField = getPrivateField("priceField", JTextField.class);
        JButton addItemButton = getPrivateField("addItemButton", JButton.class);
        JButton removeItemButton = getPrivateField("removeItemButton", JButton.class);

        productField.setText("Product A");
        quantityField.setText("1");
        priceField.setText("5.0");
        addItemButton.doClick();

        // Simulate non-numeric input.
        try (MockedStatic<JOptionPane> mockPane = Mockito.mockStatic(JOptionPane.class)) {
            mockPane.when(() -> JOptionPane.showInputDialog(any(Component.class), anyString()))
                    .thenReturn("abc");

            removeItemButton.doClick();
            mockPane.verify(() -> JOptionPane.showMessageDialog(any(Component.class), eq("Índice inválido.")),
                    times(1));
        }
    }
}
