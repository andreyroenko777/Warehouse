package com.wms.warehouse.repository;

import com.wms.warehouse.entity.Product;
import com.wms.warehouse.entity.WriteOffEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WriteOffEntryRepository extends JpaRepository<WriteOffEntry, Long> {
    @Query("SELECT COUNT(e) FROM WriteOffEntry e WHERE e.batch.product = :product")
    int countByBatchProduct(Product product);
}
