package com.example.listazakupow;
public class ShoppingItem {
    private long id;
    private String name;
    private boolean purchased;

    public ShoppingItem(long id, String name, boolean purchased) {
        this.id = id;
        this.name = name;
        this.purchased = purchased;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    @Override
    public String toString() {
        return name;
    }
}
