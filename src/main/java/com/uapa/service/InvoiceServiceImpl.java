package com.uapa.service;

import com.uapa.event.InvoiceEventPublisher;
import com.uapa.model.Invoice;
import com.uapa.repository.InvoiceRepository;
import java.util.List;

public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository repository;
    private final InvoiceEventPublisher eventPublisher;

    // Inyectamos tanto el repositorio como el publicador de eventos
    public InvoiceServiceImpl(InvoiceRepository repository, InvoiceEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void createInvoice(Invoice invoice) {
        Runnable task = () -> {
            try {
                Thread.sleep(1000); // Simula un procesamiento pesado
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            repository.saveInvoice(invoice);
            // Publicamos el evento de factura creada
            eventPublisher.publishInvoiceCreated(invoice);
        };
        new Thread(task).start();
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return repository.getAllInvoices();
    }
}
