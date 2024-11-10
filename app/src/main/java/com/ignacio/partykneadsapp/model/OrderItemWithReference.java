// OrderItemWithReference.java
package com.ignacio.partykneadsapp.model;

public class OrderItemWithReference {
    private OrderItemModel orderItem;
    private String referenceID;

    public OrderItemWithReference(OrderItemModel orderItem, String referenceID) {
        this.orderItem = orderItem;
        this.referenceID = referenceID;
    }

    public OrderItemModel getOrderItem() {
        return orderItem;
    }

    public String getReferenceID() {
        return referenceID;
    }
}
