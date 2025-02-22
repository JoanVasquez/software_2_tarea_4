package com.uapa.model;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    private String id;
    private LocalDate date;
    private List<InvoiceItem> items;
    private double total;
}
