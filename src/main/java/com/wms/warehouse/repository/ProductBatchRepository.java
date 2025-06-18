    package com.wms.warehouse.repository;

    import com.wms.warehouse.entity.Product;
    import com.wms.warehouse.entity.ProductBatch;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.time.LocalDate;
    import java.util.List;

    public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
        List<ProductBatch> findByProductOrderByExpirationDateAsc(Product product);
        List<ProductBatch> findByReceivedDateBetween(LocalDate start, LocalDate end);
        List<ProductBatch> findByQuantityGreaterThan(int qty);
        int countByProductAndQuantityGreaterThan(Product product, int quantity);


    }
