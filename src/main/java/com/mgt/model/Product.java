package com.mgt.model;

import jakarta.persistence.*;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_list_id")
	private int product_id;

	@Column(name = "productName")
	private String productName;


	@Column(name = "product_price")
	private Float product_price;

	@Column(name = "category")
	private String product_category;

	@Column(name = "stock_quantity")
	private Integer product_available_stock_quantity;

	@Column(name = "description")
	private String product_description;

	@Column(name="gst_type")
	private String gst_type;

	@Column(name="gst_rate")
	private Float gst_rate;

	@Column(name = "product_image")
	private String product_image;

	@Column(name = "product_barcode", unique = true)
	private String product_barcode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonBackReference
	private User user;

	@PrePersist
	public void generateBarcode() {
		if (this.product_barcode == null || this.product_barcode.isEmpty()) {
			this.product_barcode = "BAR-" + generateAlphanumericCode(8);
		}
	}

	private String generateAlphanumericCode(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	// Getters and Setters


	public int getProduct_id() {
		return product_id;
	}

	public void setProduct_id(int product_id) {
		this.product_id = product_id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Float getProduct_price() {
		return product_price;
	}

	public void setProduct_price(Float product_price) {
		this.product_price = product_price;
	}

	public String getProduct_category() {
		return product_category;
	}

	public void setProduct_category(String product_category) {
		this.product_category = product_category;
	}

	public Integer getProduct_available_stock_quantity() {
		return product_available_stock_quantity;
	}

	public void setProduct_available_stock_quantity(Integer product_available_stock_quantity) {
		this.product_available_stock_quantity = product_available_stock_quantity;
	}

	public String getProduct_description() {
		return product_description;
	}

	public void setProduct_description(String product_description) {
		this.product_description = product_description;
	}

	public String getGst_type() {
		return gst_type;
	}

	public void setGst_type(String gst_type) {
		this.gst_type = gst_type;
	}

	public Float getGst_rate() {
		return gst_rate;
	}

	public void setGst_rate(Float gst_rate) {
		this.gst_rate = gst_rate;
	}

	public String getProduct_image() {
		return product_image;
	}

	public void setProduct_image(String product_image) {
		this.product_image = product_image;
	}

	public String getProduct_barcode() {
		return product_barcode;
	}

	public void setProduct_barcode(String product_barcode) {
		this.product_barcode = product_barcode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
