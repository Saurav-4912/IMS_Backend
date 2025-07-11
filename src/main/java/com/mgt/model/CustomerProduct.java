package com.mgt.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name="customer_product")
public class CustomerProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private int productId;

    @Column(name="name")
    private String name;

    @Column(name="price")
    private double price;

    @Column(name="quantity")
    private int quantity;

    @Column(name="netAmount")
    private double netAmount;

    @Column(name="gstType")
    private String gstType;

    @Column(name="tax")
    private double gstRate;

    @Column(name="gstAmount")
    private double gstAmount;

    @Column(name="totalAmount")
    private double totalAmount;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonBackReference
    private Customer customer;

    public CustomerProduct(){}

    public CustomerProduct(int productId, String name, double price, int quantity, double netAmount, String gstType,
            double gstRate, double gstAmount, double totalAmount, Customer customer) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.netAmount = netAmount;
        this.gstType = gstType;
        this.gstRate = gstRate;
        this.gstAmount = gstAmount;
        this.totalAmount = totalAmount;
        this.customer = customer;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public String getGstType() {
        return gstType;
    }

    public void setGstType(String gstType) {
        this.gstType = gstType;
    }

    public double getGstRate() {
        return gstRate;
    }

    public void setGstRate(double gstRate) {
        this.gstRate = gstRate;
    }

    public double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    

  
}
