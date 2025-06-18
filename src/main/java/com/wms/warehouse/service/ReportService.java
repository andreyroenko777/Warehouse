package com.wms.warehouse.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {

    public byte[] generateReport(String type, LocalDate start, LocalDate end) {
        // Здесь будет логика выбора данных и генерации PDF
        return new byte[0]; // Заглушка
    }
}
