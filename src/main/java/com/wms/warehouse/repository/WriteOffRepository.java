package com.wms.warehouse.repository;

import com.wms.warehouse.entity.ProductBatch;
import com.wms.warehouse.entity.WriteOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WriteOffRepository extends JpaRepository<WriteOff, Long> {


    List<WriteOff> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<WriteOff> findByReasonAndDateTimeBetween(String reason, LocalDateTime start, LocalDateTime end);
}

