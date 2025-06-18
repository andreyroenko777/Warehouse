package com.wms.warehouse.entity;

public class WriteOffForm {
    private Long productId;
    private int quantity;
    private WriteOffReason reason;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public WriteOffReason getReason() { return reason; }
    public void setReason(WriteOffReason reason) { this.reason = reason; }
}
