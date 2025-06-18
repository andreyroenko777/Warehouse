package com.wms.warehouse.controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import com.wms.warehouse.entity.ProductBatch;
import com.wms.warehouse.entity.WriteOff;
import com.wms.warehouse.entity.WriteOffReason;
import com.wms.warehouse.repository.ProductBatchRepository;
import com.wms.warehouse.repository.WriteOffRepository;
import com.wms.warehouse.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final WriteOffRepository writeOffRepository;
    private final ProductBatchRepository batchRepository;
    private PdfPCell makeCell(String text, Font font) {
        return makeCell(text, font, Element.ALIGN_LEFT);
    }

    private PdfPCell makeCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5f);
        return cell;
    }


    public ReportController(WriteOffRepository writeOffRepository,
                            ProductBatchRepository batchRepository) {
        this.writeOffRepository = writeOffRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping
    public String reportForm() {
        return "reports";
    }

    @PostMapping("/generate")
    public void generateReport(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                               @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                               @RequestParam("reportType") String reportType,
                               HttpServletResponse response) throws IOException, DocumentException {

        response.setContentType("application/pdf");
        // Преобразуем тип отчёта в человекочитаемый

        Document document;
        Font smallFont;
        try (OutputStream out = response.getOutputStream()) {
            document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font rowFont = new Font(Font.HELVETICA, 11);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            smallFont = new Font(Font.HELVETICA, 9, Font.ITALIC);
            if (reportType.equals("ARRIVALS")) {
                List<ProductBatch> batches = batchRepository.findByReceivedDateBetween(start, end);
                String fileName = String.format("report_arrivals_%s_to_%s.pdf", start, end);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

                document.add(new Paragraph("Отчёт о поступлениях", titleFont));
                document.add(new Paragraph("Период: " + start + " — " + end));
                document.add(new Paragraph(" "));

                if (batches.isEmpty()) {
                    document.add(new Paragraph("Нет данных о поступлениях за указанный период.", rowFont));
                } else {
                    PdfPTable table = new PdfPTable(new float[]{4, 2, 3, 3});
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10f);
                    table.setSpacingAfter(10f);

                    Stream.of("Товар", "Кол-во", "Дата прихода", "Срок годности")
                            .forEach(title -> {
                                PdfPCell header = new PdfPCell(new Phrase(title, headerFont));
                                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                                header.setBackgroundColor(new Color(220, 220, 220));
                                header.setPadding(5f);
                                table.addCell(header);
                            });

                    int total = 0;

                    for (ProductBatch batch : batches) {
                        table.addCell(makeCell(batch.getProduct().getName(), rowFont));
                        table.addCell(makeCell(String.valueOf(batch.getInitialQuantity()), rowFont, Element.ALIGN_RIGHT));
                        table.addCell(makeCell(batch.getReceivedDate().toString(), rowFont));
                        table.addCell(makeCell(batch.getExpirationDate() != null
                                ? batch.getExpirationDate().toString() : "—", rowFont));
                        total += batch.getQuantity();
                    }

                    // Итоговая строка
                    PdfPCell totalLabel = new PdfPCell(new Phrase("Итого", headerFont));
                    totalLabel.setColspan(1);
                    totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalLabel.setPadding(5f);
                    table.addCell(totalLabel);

                    PdfPCell totalValue = new PdfPCell(new Phrase(String.valueOf(total), headerFont));
                    totalValue.setColspan(3);
                    totalValue.setHorizontalAlignment(Element.ALIGN_LEFT);
                    totalValue.setPadding(5f);
                    table.addCell(totalValue);

                    document.add(table);
                }

                document.add(new Paragraph("Сформировано системой WMS, " + LocalDate.now(), smallFont));
                document.close();
                return; // Завершаем, чтобы не продолжать к другим типам отчёта
            }




            WriteOffReason targetReason = WriteOffReason.valueOf(reportType);
            String reportTitle = targetReason == WriteOffReason.DISPOSAL ? "Отчёт по утилизации" : "Отчёт по списанию";

// <-- ДОБАВЬ ВОТ ЭТО
            String fileLabel = (targetReason == WriteOffReason.DISPOSAL) ? "utilization" : "writeoff";
            String fileName = String.format("report_%s_%s_to_%s.pdf", fileLabel, start, end);

            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            List<WriteOff> writeOffs = writeOffRepository.findByDateTimeBetween(
                            start.atStartOfDay(), end.atTime(23, 59))
                    .stream()
                    .filter(w -> w.getReason() == targetReason)
                    .toList();

            document.add(new Paragraph(reportTitle, titleFont));
            document.add(new Paragraph("Период: " + start + " — " + end));
            document.add(new Paragraph(" "));

            if (writeOffs.isEmpty()) {
                document.add(new Paragraph("Нет записей за выбранный период.", rowFont));
            } else {
                PdfPTable table = new PdfPTable(new float[]{3, 3, 2, 2, 3}); // Пропорции ширин колонок
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Заголовки таблицы
                Stream.of("Дата и время", "Товар", "Причина", "Количество", "Пользователь")
                        .forEach(title -> {
                            PdfPCell header = new PdfPCell(new Phrase(title, headerFont));
                            header.setHorizontalAlignment(Element.ALIGN_CENTER);
                            header.setBackgroundColor(new Color(230, 230, 230));
                            header.setPadding(5f);
                            table.addCell(header);
                        });

                int totalSum = 0;

                for (WriteOff w : writeOffs) {
                    table.addCell(makeCell(w.getDateTime().toString(), rowFont));
                    table.addCell(makeCell(w.getProduct().getName(), rowFont));
                    table.addCell(makeCell(w.getReason().toString(), rowFont));
                    table.addCell(makeCell(String.valueOf(w.getTotalQuantity()), rowFont, Element.ALIGN_RIGHT));
                    table.addCell(makeCell(w.getUsername(), rowFont));
                    totalSum += w.getTotalQuantity();
                }

                // Добавим итоговую строку
                PdfPCell totalLabelCell = new PdfPCell(new Phrase("Итого", headerFont));
                totalLabelCell.setColspan(3);
                totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalLabelCell.setPadding(5f);
                table.addCell(totalLabelCell);

                PdfPCell totalValueCell = new PdfPCell(new Phrase(String.valueOf(totalSum), headerFont));
                totalValueCell.setColspan(2);
                totalValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                totalValueCell.setPadding(5f);
                table.addCell(totalValueCell);

                document.add(table);
            }

            document.add(new Paragraph("Сформировано системой WMS, " + LocalDate.now(), smallFont));
            document.close();


        }


    }
}




