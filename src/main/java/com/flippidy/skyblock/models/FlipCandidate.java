package com.flippidy.skyblock.models;

public class FlipCandidate {
    private BazaarItem item;
    private double profitPerItem;
    private double profitMargin;
    private int maxItems;
    private double totalInvestment;
    private double totalProfit;

    public FlipCandidate(BazaarItem item, double profitPerItem, double profitMargin, int maxItems, double totalInvestment, double totalProfit) {
        this.item = item;
        this.profitPerItem = profitPerItem;
        this.profitMargin = profitMargin;
        this.maxItems = maxItems;
        this.totalInvestment = totalInvestment;
        this.totalProfit = totalProfit;
    }

    public BazaarItem getItem() {
        return item;
    }

    public double getProfitPerItem() {
        return profitPerItem;
    }

    public double getProfitMargin() {
        return profitMargin;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public double getTotalInvestment() {
        return totalInvestment;
    }

    public double getTotalProfit() {
        return totalProfit;
    }
}