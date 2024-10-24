package com.example.cats_catch_mice.ui.itemList;

public class Item {
    private String name;
    private String description;
    private int count;
    private int imageResId;  // New field for image resource

    public Item(String name, String description, int count, int imageResId) {
        this.name = name;
        this.description = description;
        this.count = count;
        this.imageResId = imageResId;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getImageResId() {
        return imageResId;
    }
}
