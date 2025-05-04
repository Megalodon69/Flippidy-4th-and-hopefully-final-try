package com.flippidy.skyblock.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private String name;
    private Map<String, Boolean> filterSettings;
    private double minProfitMargin;
    private double maxInvestmentPercentage;
    private double maxEventInvestmentPercentage;
    private List<String> favoriteItems;
    private List<String> blacklistedItems;
    
    public UserProfile(String name) {
        this.name = name;
        this.filterSettings = new HashMap<>();
        this.minProfitMargin = 1.0; // Standardwert: 1% Mindestgewinn
        this.maxInvestmentPercentage = 30.0; // Standardwert: 30% des Vermögens
        this.maxEventInvestmentPercentage = 70.0; // Standardwert für Events: 70% des Vermögens
        this.favoriteItems = new ArrayList<>();
        this.blacklistedItems = new ArrayList<>();
        
        // Standard-Filtereinstellungen
        filterSettings.put("showUnstableItems", true);
        filterSettings.put("showLowVolumeItems", false);
        filterSettings.put("prioritizeFavorites", true);
    }
    
    public static UserProfile load(File file) {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, UserProfile.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, Boolean> getFilterSettings() {
        return filterSettings;
    }
    
    public void setFilterSetting(String key, boolean value) {
        filterSettings.put(key, value);
    }
    
    public double getMinProfitMargin() {
        return minProfitMargin;
    }
    
    public void setMinProfitMargin(double minProfitMargin) {
        this.minProfitMargin = minProfitMargin;
    }
    
    public double getMaxInvestmentPercentage() {
        return maxInvestmentPercentage;
    }
    
    public void setMaxInvestmentPercentage(double maxInvestmentPercentage) {
        this.maxInvestmentPercentage = maxInvestmentPercentage;
    }
    
    public double getMaxEventInvestmentPercentage() {
        return maxEventInvestmentPercentage;
    }
    
    public void setMaxEventInvestmentPercentage(double maxEventInvestmentPercentage) {
        this.maxEventInvestmentPercentage = maxEventInvestmentPercentage;
    }
    
    public List<String> getFavoriteItems() {
        return favoriteItems;
    }
    
    public void addFavoriteItem(String itemId) {
        if (!favoriteItems.contains(itemId)) {
            favoriteItems.add(itemId);
        }
    }
    
    public void removeFavoriteItem(String itemId) {
        favoriteItems.remove(itemId);
    }
    
    public List<String> getBlacklistedItems() {
        return blacklistedItems;
    }
    
    public void addBlacklistedItem(String itemId) {
        if (!blacklistedItems.contains(itemId)) {
            blacklistedItems.add(itemId);
        }
    }
    
    public void removeBlacklistedItem(String itemId) {
        blacklistedItems.remove(itemId);
    }
    
    public boolean isItemBlacklisted(String itemId) {
        return blacklistedItems.contains(itemId);
    }
    
    public boolean isItemFavorite(String itemId) {
        return favoriteItems.contains(itemId);
    }
}