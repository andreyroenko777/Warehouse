package com.wms.warehouse.controller;

import com.wms.warehouse.entity.Product;
import com.wms.warehouse.entity.ProductBatch;
import com.wms.warehouse.repository.ProductRepository;
import com.wms.warehouse.repository.ProductBatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StockController {

    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;

    public StockController(ProductRepository productRepository, ProductBatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping("/stock")
    public String showStock(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(7);

        List<Product> products = productRepository.findAll();
        List<ProductBatch> batches = batchRepository.findByQuantityGreaterThan(0);


        Map<Long, Integer> total = new HashMap<>();
        Map<Long, Integer> expired = new HashMap<>();
        Map<Long, Integer> expiring = new HashMap<>();
        Map<Long, List<ProductBatch>> batchesByProduct = new HashMap<>();

        for (ProductBatch b : batches) {
            Long id = b.getProduct().getId();
            batchesByProduct.computeIfAbsent(id, k -> new ArrayList<>()).add(b);

            if (b.getExpirationDate() != null) {
                if (b.getExpirationDate().isBefore(today)) {
                    expired.merge(id, b.getQuantity(), Integer::sum);
                } else if (!b.getExpirationDate().isAfter(soon)) {
                    expiring.merge(id, b.getQuantity(), Integer::sum);
                    total.merge(id, b.getQuantity(), Integer::sum);
                } else {
                    total.merge(id, b.getQuantity(), Integer::sum);
                }
            } else {
                total.merge(id, b.getQuantity(), Integer::sum);
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("total", total);
        model.addAttribute("expired", expired);
        model.addAttribute("expiring", expiring);
        model.addAttribute("batchesByProduct", batchesByProduct);

        return "stock";
    }
}
