package com.wms.warehouse.controller;

import com.wms.warehouse.entity.Product;
import com.wms.warehouse.repository.ProductBatchRepository;
import com.wms.warehouse.repository.ProductRepository;

import com.wms.warehouse.repository.WriteOffEntryRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.BindingResult;

import java.util.List;
@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;
    private final WriteOffEntryRepository entryRepository;

    public ProductController(ProductRepository productRepository,
                             ProductBatchRepository batchRepository,
                             WriteOffEntryRepository entryRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.entryRepository = entryRepository;
    }


    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("units", List.of("шт", "кг", "л", "упак")); // список единиц
        return "product_form";
    }

    @PostMapping("/products")
    public String createProduct(@ModelAttribute @Valid Product product, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("units", List.of("шт", "кг", "л", "упак"));
            return "product_form";
        }

        productRepository.save(product);
        return "redirect:/products";
    }
    @GetMapping("/products")
    public String getAllProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + id));
        model.addAttribute("product", product);
        model.addAttribute("units", List.of("шт", "кг", "л", "упак"));
        return "product_form"; // тот же шаблон, что и при создании
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute @Valid Product product,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("units", List.of("шт", "кг", "л", "упак"));
            return "product_form";
        }

        product.setId(id); // важно установить ID перед сохранением
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        int activeBatchCount = batchRepository.countByProductAndQuantityGreaterThan(product, 0);
        int writeOffLinks = entryRepository.countByBatchProduct(product);

        if (activeBatchCount > 0 || writeOffLinks > 0) {
            model.addAttribute("error", "Невозможно удалить товар: есть партии с остатками или он использовался в списаниях.");
            model.addAttribute("products", productRepository.findAll());
            return "products";
        }

        // Удалим только те партии, которые пустые и не участвуют в списаниях
        batchRepository.deleteAll(batchRepository.findByProductOrderByExpirationDateAsc(product));
        productRepository.delete(product);

        return "redirect:/products";
    }



}
