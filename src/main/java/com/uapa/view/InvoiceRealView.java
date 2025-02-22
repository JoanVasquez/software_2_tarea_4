package com.uapa.view;

import com.uapa.controller.InvoiceController;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import com.uapa.observer.InvoiceGUIObserver;
import com.uapa.observer.InvoiceObserver;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvoiceRealView extends JFrame {
    private final InvoiceController controller;
    private final JTextField productField;
    private final JTextField quantityField;
    private final JTextField priceField;
    private final JTextField dateField;
    private final JTextField totalField;
    private final JButton addItemButton;
    private final JButton createInvoiceButton;
    private final JButton viewInvoicesButton;
    private final JButton removeItemButton; // Botón para eliminar un ítem pendiente
    private final JTextArea itemsArea;
    private final List<InvoiceItem> items;
    // Observador para el área de log
    private final InvoiceObserver guiObserver;

    public InvoiceRealView(InvoiceController controller) {
        this.controller = controller;
        this.items = new ArrayList<>();

        // Configurar Look and Feel Nimbus, si está disponible
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        setTitle("Generar Factura Real");
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel de entrada de datos
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Producto
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Producto:"), gbc);
        gbc.gridx = 1;
        productField = new JTextField(15);
        inputPanel.add(productField, gbc);

        // Fila 1: Cantidad
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(15);
        inputPanel.add(quantityField, gbc);

        // Fila 2: Precio
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Precio:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(15);
        inputPanel.add(priceField, gbc);

        // Fila 3: Fecha (formato yyyy-MM-dd)
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Fecha (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setText(LocalDate.now().toString());
        inputPanel.add(dateField, gbc);

        // Fila 4: Total (campo calculado y deshabilitado)
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Total:"), gbc);
        gbc.gridx = 1;
        totalField = new JTextField(15);
        totalField.setEditable(false);
        totalField.setText("0.0");
        inputPanel.add(totalField, gbc);

        // Fila 5: Botones Agregar Ítem y Crear Factura
        gbc.gridx = 0;
        gbc.gridy = 5;
        addItemButton = new JButton("Agregar Producto");
        inputPanel.add(addItemButton, gbc);
        gbc.gridx = 1;
        createInvoiceButton = new JButton("Crear Factura");
        inputPanel.add(createInvoiceButton, gbc);

        // Fila 6: Botones Visualizar Facturas y Eliminar Ítem
        gbc.gridx = 0;
        gbc.gridy = 6;
        viewInvoicesButton = new JButton("Visualizar Facturas");
        inputPanel.add(viewInvoicesButton, gbc);
        gbc.gridx = 1;
        removeItemButton = new JButton("Eliminar Ítem");
        inputPanel.add(removeItemButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Área para mostrar los ítems agregados
        itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        itemsArea.setBackground(new Color(230, 230, 230));
        itemsArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane scrollPane = new JScrollPane(itemsArea);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // DocumentListener para actualizar el total al editar cantidad o precio
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTotalPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTotalPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTotalPreview();
            }
        };
        quantityField.getDocument().addDocumentListener(dl);
        priceField.getDocument().addDocumentListener(dl);

        // Configuración de listeners de botones
        addItemButton.addActionListener(this::handleAddItem);
        createInvoiceButton.addActionListener(this::handleCreateInvoice);
        viewInvoicesButton.addActionListener(e -> {
            InvoiceListView listView = new InvoiceListView(controller);
            listView.setVisible(true);
        });
        removeItemButton.addActionListener(this::handleRemoveItem);

        // Inicializamos el observador y lo dejamos accesible mediante el getter
        this.guiObserver = new InvoiceGUIObserver(itemsArea);
    }

    private void updateItemsArea() {
        itemsArea.setText("");
        double total = 0.0;
        for (InvoiceItem item : items) {
            itemsArea.append(String.format(
                    "Producto: %s, Cantidad: %d, Precio: %.2f%n",
                    item.getProduct(),
                    item.getQuantity(),
                    item.getPrice()));
            total += item.getQuantity() * item.getPrice();
        }
        // Eliminar el bloque que suma qty y price de los campos de texto
        // Deja únicamente la asignación del total calculado
        totalField.setText(String.valueOf(total));
    }

    private void updateTotalPreview() {
        double total = 0.0;
        for (InvoiceItem item : items) {
            total += item.getQuantity() * item.getPrice();
        }
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            total += qty * price;
        } catch (NumberFormatException e) {
            // Ignorar si los valores no son válidos
        }
        totalField.setText(String.valueOf(total));
    }

    private void handleAddItem(ActionEvent e) {
        String product = productField.getText().trim();
        if (product.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del producto.");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Precio inválido.");
            return;
        }
        InvoiceItem item = InvoiceItem.builder()
                .product(product)
                .quantity(quantity)
                .price(price)
                .build();
        items.add(item);
        updateItemsArea();
        // Limpiar campos de entrada (mantener fecha y total)
        productField.setText("");
        quantityField.setText("");
        priceField.setText("");
    }

    private void handleCreateInvoice(ActionEvent e) {
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un ítem.");
            return;
        }

        try {
            LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Fecha inválida. Use el formato yyyy-MM-dd.");
            return;
        }

        controller.generateRealInvoice(items);
        JOptionPane.showMessageDialog(this, "Factura creada con éxito.");
        items.clear();
        updateItemsArea();
    }

    private void handleRemoveItem(ActionEvent e) {
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ítems para eliminar.");
            return;
        }
        StringBuilder sb = new StringBuilder("Ítems disponibles:\n");
        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i);
            sb.append(String.format("%d: Producto: %s, Cantidad: %d, Precio: %.2f%n",
                    i, item.getProduct(), item.getQuantity(), item.getPrice()));
        }
        String input = JOptionPane.showInputDialog(this, sb.toString() + "\nIngrese el índice del ítem a eliminar:");
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un índice válido.");
            return;
        }
        try {
            int index = Integer.parseInt(input.trim());
            if (index < 0 || index >= items.size()) {
                JOptionPane.showMessageDialog(this, "Índice fuera de rango.");
                return;
            }
            items.remove(index);
            updateItemsArea();
            JOptionPane.showMessageDialog(this, "Ítem eliminado con éxito.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Índice inválido.");
        }
    }

    // Getter para el observador que actualiza el área de log.
    public InvoiceObserver getLogAreaObserver() {
        return guiObserver;
    }

}
