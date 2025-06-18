package com.wms.warehouse.entity;

import jakarta.persistence.*;

@Entity
public class WriteOffEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private WriteOff writeOff;

    @ManyToOne(optional = false)
    private ProductBatch batch;

    private int quantity;

    public WriteOffEntry() {}

    // Getters & Setters
    public Long getId() { return id; }

    public WriteOff getWriteOff() { return writeOff; }
    public void setWriteOff(WriteOff writeOff) { this.writeOff = writeOff; }

    public ProductBatch getBatch() { return batch; }
    public void setBatch(ProductBatch batch) { this.batch = batch; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
