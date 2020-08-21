/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DailyPurchase;

/**
 *
 * @author hajar
 */
public class Product {
    private int id;
    private String name;
    private float price;
    private String addDate;
    private byte[] picture;

    public Product(int id, String name, float price, String addDate, byte[] picture) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.addDate = addDate;
        this.picture = picture;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public String getAddDate() {
        return addDate;
    }

    public byte[] getPicture() {
        return picture;
    }
    
    
}
