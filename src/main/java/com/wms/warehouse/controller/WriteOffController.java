package com.wms.warehouse.controller;

import com.wms.warehouse.entity.*;
import com.wms.warehouse.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/write-off")
public class WriteOffController {

    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;
    private final WriteOffRepository writeOffRepository;
    private final WriteOffEntryRepository entryRepository;

    public WriteOffController(ProductRepository productRepository,
                              ProductBatchRepository batchRepository,
                              WriteOffRepository writeOffRepository,
                              WriteOffEntryRepository entryRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.writeOffRepository = writeOffRepository;
        this.entryRepository = entryRepository;
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("writeOff", new WriteOffForm());
        model.addAttribute("reasons", WriteOffReason.values());
        return "write_off_form";
    }

    @PostMapping
    public String processForm(@ModelAttribute WriteOffForm form,
                              Authentication auth,
                              Model model) {
        Optional<Product> productOpt = productRepository.findById(form.getProductId());
        if (productOpt.isEmpty()) {
            model.addAttribute("error", "Товар не найден");
            return "write_off_form";
        }

        Product product = productOpt.get();
        int remaining = form.getQuantity();
        LocalDate today = LocalDate.now();

        List<ProductBatch> allBatches = batchRepository.findByProductOrderByExpirationDateAsc(product);

        List<ProductBatch> expiredBatches = new ArrayList<>();
        List<ProductBatch> validBatches = new ArrayList<>();

        for (ProductBatch batch : allBatches) {
            if (batch.getQuantity() <= 0) continue;

            if (batch.getExpirationDate() != null && batch.getExpirationDate().isBefore(today)) {
                expiredBatches.add(batch);
            } else {
                validBatches.add(batch);
            }
        }

        List<WriteOffEntry> entries = new ArrayList<>();

        if (form.getReason() == WriteOffReason.SALE) {
            int validTotal = validBatches.stream().mapToInt(ProductBatch::getQuantity).sum();
            if (validTotal < remaining) {
                model.addAttribute("error", "Недостаточно годного товара для продажи");
                model.addAttribute("products", productRepository.findAll());
                model.addAttribute("reasons", WriteOffReason.values());
                return "write_off_form";
            }

            for (ProductBatch batch : validBatches) {
                if (remaining == 0) break;
                int toWriteOff = Math.min(batch.getQuantity(), remaining);
                batch.setQuantity(batch.getQuantity() - toWriteOff);
                batchRepository.save(batch); // просто сохраняем, даже если 0

                WriteOffEntry entry = new WriteOffEntry();
                entry.setBatch(batch);
                entry.setQuantity(toWriteOff);
                entries.add(entry);

                remaining -= toWriteOff;
            }

        } else if (form.getReason() == WriteOffReason.DISPOSAL) {
            for (ProductBatch batch : expiredBatches) {
                if (remaining == 0) break;
                int toWriteOff = Math.min(batch.getQuantity(), remaining);
                batch.setQuantity(batch.getQuantity() - toWriteOff);
                batchRepository.save(batch);

                WriteOffEntry entry = new WriteOffEntry();
                entry.setBatch(batch);
                entry.setQuantity(toWriteOff);
                entries.add(entry);

                remaining -= toWriteOff;
            }

            for (ProductBatch batch : validBatches) {
                if (remaining == 0) break;
                int toWriteOff = Math.min(batch.getQuantity(), remaining);
                batch.setQuantity(batch.getQuantity() - toWriteOff);
                batchRepository.save(batch);

                WriteOffEntry entry = new WriteOffEntry();
                entry.setBatch(batch);
                entry.setQuantity(toWriteOff);
                entries.add(entry);

                remaining -= toWriteOff;
            }

            if (remaining > 0) {
                model.addAttribute("error", "Недостаточно товара для утилизации");
                model.addAttribute("products", productRepository.findAll());
                model.addAttribute("reasons", WriteOffReason.values());
                return "write_off_form";
            }
        }

        WriteOff writeOff = new WriteOff();
        writeOff.setProduct(product);
        writeOff.setDateTime(LocalDateTime.now());
        writeOff.setReason(form.getReason());
        writeOff.setTotalQuantity(form.getQuantity());
        writeOff.setUsername(auth.getName());

        writeOff = writeOffRepository.save(writeOff);

        for (WriteOffEntry entry : entries) {
            entry.setWriteOff(writeOff);
            entryRepository.save(entry);
        }

        return "redirect:/stock";
    }

}
