package com.wms.warehouse.controller;

import com.wms.warehouse.entity.Product;
import com.wms.warehouse.entity.ProductBatch;
import com.wms.warehouse.repository.ProductRepository;
import com.wms.warehouse.repository.ProductBatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ProductBatchController {

    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;

    public ProductBatchController(ProductRepository productRepository, ProductBatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping("/batches/new")
    public String showCreateForm(Model model) {
        model.addAttribute("batch", new ProductBatch());
        model.addAttribute("products", productRepository.findAll());
        return "batch_form";
    }

    @PostMapping("/batches")
    public String createBatch(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("expirationDate") String expirationDateStr
    ) {
        Product product = productRepository.findById(productId).orElseThrow();
        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setQuantity(quantity);
        batch.setInitialQuantity(quantity); // <-- ВАЖНО!
        batch.setReceivedDate(LocalDate.now());
        batch.setExpirationDate(LocalDate.parse(expirationDateStr)); // yyyy-MM-dd
        batchRepository.save(batch);
        return "redirect:/stock";
    }

    @GetMapping("/batches")
    public String listBatches(Model model) {
        model.addAttribute("batches", batchRepository.findAll());
        return "batch_list";
    }
}
