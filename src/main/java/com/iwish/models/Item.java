package com.iwish.models;

public class Item {
    private int id;
    private String name;
    private double price;
    private String description;
    private String imgSrc;
    private double funded;

    public Item() {
    }

    // Constructor for marketplace display
    public Item(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imgSrc = "";
    }

    // Constructor with image
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getFunded() {
        return funded;
    }

    public void setFunded(double funded) {
        this.funded = funded;
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
