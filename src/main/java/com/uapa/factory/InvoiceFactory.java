package com.uapa.factory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;

public class InvoiceFactory {
        public static Invoice createDummyInvoice() {
                InvoiceItem item1 = InvoiceItem.builder()
                                .product("Producto A")
                                .quantity(2)
                                .price(15.0)
                                .build();

                InvoiceItem item2 = InvoiceItem.builder()
                                .product("Producto B")
                                .quantity(1)
                                .price(30.0)
                                .build();

                List<InvoiceItem> items = List.of(item1, item2);
                double total = items.stream()
                                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                                .sum();

                return Invoice.builder()
                                .id(UUID.randomUUID().toString())
                                .date(LocalDate.now())
                                .items(items)
                                .total(total)

                                .build();
        }

        public static Invoice createInvoice(List<InvoiceItem> items) {
                double total = items.stream()
                                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                                .sum();
                return Invoice.builder()
                                .id(UUID.randomUUID().toString())
                                .date(LocalDate.now())
                                .items(items)
                                .total(total)
                                .build();
        }

}
