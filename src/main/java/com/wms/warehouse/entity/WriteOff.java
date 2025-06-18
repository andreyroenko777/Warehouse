package com.wms.warehouse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class WriteOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    private WriteOffReason reason;

    private LocalDateTime dateTime;

    private int totalQuantity;

    private String username;

    @OneToMany(mappedBy = "writeOff", cascade = CascadeType.ALL)
    private List<WriteOffEntry> entries;

    public WriteOff() {}

    // Getters & Setters
    public Long getId() { return id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public WriteOffReason getReason() { return reason; }
    public void setReason(WriteOffReason reason) { this.reason = reason; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<WriteOffEntry> getEntries() { return entries; }
    public void setEntries(List<WriteOffEntry> entries) { this.entries = entries; }
}
