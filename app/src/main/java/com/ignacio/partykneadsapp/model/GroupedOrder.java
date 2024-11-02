package com.ignacio.partykneadsapp.model;

import java.util.ArrayList;
import java.util.List;

public class GroupedOrder {
    private List<OrderItem> orderItems; // List of individual items
    private String userName;
    private String contactNum;
    private String location;

    public GroupedOrder(String userName, String contactNum, String location) {
        this.orderItems = new ArrayList<>(); // Initialize the list
        this.userName = userName;
        this.contactNum = contactNum;
        this.location = location;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public String getUserName() {
        return userName;
    }

    public String getContactNum() {
        return contactNum;
    }

    public String getLocation() {
        return location;
    }
}
