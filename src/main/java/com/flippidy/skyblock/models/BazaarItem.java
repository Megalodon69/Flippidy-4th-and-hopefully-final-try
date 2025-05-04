package com.flippidy.skyblock.models;

public class BazaarItem {
    private String id;
    private String productId;
    private double bestBuyPrice;  // Der höchste Preis, zu dem Spieler kaufen wollen
    private double bestSellPrice; // Der niedrigste Preis, zu dem Spieler verkaufen wollen
    private int buyVolume;        // Anzahl der Items in Kaufaufträgen
    private int sellVolume;       // Anzahl der Items in Verkaufsaufträgen

    public BazaarItem(String id, String productId, double bestBuyPrice, double bestSellPrice, int buyVolume, int sellVolume) {
        this.id = id;
        this.productId = productId;
        this.bestBuyPrice = bestBuyPrice;
        this.bestSellPrice = bestSellPrice;
        this.buyVolume = buyVolume;
        this.sellVolume = sellVolume;
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public double getBestBuyPrice() {
        return bestBuyPrice;
    }

    public double getBestSellPrice() {
        return bestSellPrice;
    }

    public int getBuyVolume() {
        return buyVolume;
    }

    public int getSellVolume() {
        return sellVolume;
    }
}