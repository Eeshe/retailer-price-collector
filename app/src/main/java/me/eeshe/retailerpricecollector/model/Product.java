package me.eeshe.retailerpricecollector.model;

public class Product {
  private final String barCode;
  private final String categoryId;
  private final String name;
  private final double price;

  public Product(String barCode, String categoryId, String name, double price) {
    this.barCode = barCode;
    this.categoryId = categoryId;
    this.name = name;
    this.price = price;
  }

  public String getBarCode() {
    return barCode;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public String getName() {
    return name;
  }

  public double getPrice() {
    return price;
  }
}
