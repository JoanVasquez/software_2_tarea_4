package com.uapa.event;

import com.uapa.model.Invoice;
import com.uapa.observer.InvoiceObserver;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleInvoiceEventPublisher implements InvoiceEventPublisher {
    private final List<InvoiceObserver> observers = new CopyOnWriteArrayList<>();

    public void registerObserver(InvoiceObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(InvoiceObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void publishInvoiceCreated(Invoice invoice) {
        for (InvoiceObserver observer : observers) {
            observer.update(invoice);
        }
    }
}
