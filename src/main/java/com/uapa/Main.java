package com.uapa;

import javax.swing.SwingUtilities;
import com.uapa.config.DatabaseConnector;
import com.uapa.config.H2DatabaseConnector;
import com.uapa.controller.InvoiceController;
import com.uapa.event.InvoiceEventPublisher;
import com.uapa.event.SimpleInvoiceEventPublisher;
import com.uapa.repository.InvoiceRepository;
import com.uapa.repository.InvoiceRepositoryImpl;
import com.uapa.service.InvoiceService;
import com.uapa.service.InvoiceServiceImpl;
import com.uapa.view.InvoiceRealView;

public class Main {
    public static void main(String[] args) {
        DatabaseConnector connector = new H2DatabaseConnector();
        InvoiceRepository repository = new InvoiceRepositoryImpl(connector);
        // Creamos el publicador de eventos
        InvoiceEventPublisher eventPublisher = new SimpleInvoiceEventPublisher();
        InvoiceService service = new InvoiceServiceImpl(repository, eventPublisher);
        InvoiceController controller = new InvoiceController(service);

        SwingUtilities.invokeLater(() -> {
            InvoiceRealView view = new InvoiceRealView(controller);
            // La vista se suscribe al publicador de eventos
            // (La vista no conoce la implementación del publicador; esto se puede hacer
            // aquí o dentro de la vista si se inyecta el publicador)
            ((SimpleInvoiceEventPublisher) eventPublisher).registerObserver(view.getLogAreaObserver());
            view.setVisible(true);
        });
    }
}
