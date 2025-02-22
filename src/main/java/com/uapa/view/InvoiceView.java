package com.uapa.view;

import javax.swing.*;
import java.awt.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.uapa.controller.InvoiceController;

public class InvoiceView extends JFrame {
    private final InvoiceController controller;
    private final JButton generateButton;
    private final JTextArea logArea;

    public InvoiceView(InvoiceController controller) {
        this.controller = controller;
        setTitle("Sistema de Facturación");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelTop = new JPanel();
        generateButton = new JButton("Generar Factura");
        panelTop.add(generateButton);
        add(panelTop, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> controller.generateInvoice());
    }

    public JTextArea getLogArea() {
        return logArea;
    }
}
