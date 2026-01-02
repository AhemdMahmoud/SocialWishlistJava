package com.iwish.models;

import java.math.BigDecimal; // This import is no longer needed if BigDecimal is removed

public class Item {
    private int id;
    private String name;
    private double price; // Changed from BigDecimal to double
    private String description;
    private String imgSrc; // New field

    public Item() {
    }

    // Original constructor removed as price type changed and new constructors are provided
    // public Item(int id, String name, BigDecimal price, String description) {
    //     this.id = id;
    //     this.name = name;
    //     this.price = price;
    //     this.description = description;
    // }

    // New constructor
    public Item(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imgSrc = ""; // Default empty
    }

    // New constructor
    public Item(int id, String name, double price, String imgSrc) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imgSrc = imgSrc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() { // Changed return type
        return price;
    }

    public void setPrice(double price) { // Changed parameter type
        this.price = price;
    }

    public String getImgSrc() { // New getter
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) { // New setter
        this.imgSrc = imgSrc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
