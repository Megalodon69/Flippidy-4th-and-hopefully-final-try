package com.flippidy.skyblock.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FlipStatistics {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final File statisticsFile;
    private Map<String, List<FlipRecord>> dailyFlips;
    private Map<String, Double> dailyProfits;
    private Map<String, Integer> itemFlipCounts;
    private double totalProfit;
    private int totalFlips;
    private Date firstFlipDate;
    private Date lastFlipDate;

    public FlipStatistics(File statisticsFile) {
        this.statisticsFile = statisticsFile;
        this.dailyFlips = new HashMap<>();
        this.dailyProfits = new HashMap<>();
        this.itemFlipCounts = new HashMap<>();
        this.totalProfit = 0.0;
        this.totalFlips = 0;
        this.firstFlipDate = null;
        this.lastFlipDate = null;
    }

    public void addFlip(FlipRecord flip) {
        // Aktualisiere tägliche Flips
        String dateKey = DATE_FORMAT.format(flip.getTimestamp());
        if (!dailyFlips.containsKey(dateKey)) {
            dailyFlips.put(dateKey, new ArrayList<>());
        }
        dailyFlips.get(dateKey).add(flip);

        // Aktualisiere täglichen Profit
        if (!dailyProfits.containsKey(dateKey)) {
            dailyProfits.put(dateKey, 0.0);
        }
        dailyProfits.put(dateKey, dailyProfits.get(dateKey) + flip.getTotalProfit());

        // Aktualisiere Item-Flip-Zähler
        if (!itemFlipCounts.containsKey(flip.getItemId())) {
            itemFlipCounts.put(flip.getItemId(), 0);
        }
        itemFlipCounts.put(flip.getItemId(), itemFlipCounts.get(flip.getItemId()) + 1);

        // Aktualisiere Gesamtstatistiken
        totalProfit += flip.getTotalProfit();
        totalFlips++;

        // Aktualisiere Datumsbereiche
        Date flipDate = flip.getTimestamp();
        if (firstFlipDate == null || flipDate.before(firstFlipDate)) {
            firstFlipDate = flipDate;
        }
        if (lastFlipDate == null || flipDate.after(lastFlipDate)) {
            lastFlipDate = flipDate;
        }

        // Speichere nach jeder Änderung
        save();
    }

    public void addProfit(double profit) {
        // Aktualisiere den heutigen Profit ohne einen spezifischen Flip zu erfassen
        String today = DATE_FORMAT.format(new Date());
        if (!dailyProfits.containsKey(today)) {
            dailyProfits.put(today, 0.0);
        }
        dailyProfits.put(today, dailyProfits.get(today) + profit);
        
        // Aktualisiere Gesamtprofit
        totalProfit += profit;
        
        // Update das letzte Flip-Datum
        lastFlipDate = new Date();
        
        // Aktualisiere das erste Flip-Datum, falls nötig
        if (firstFlipDate == null) {
            firstFlipDate = lastFlipDate;
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(statisticsFile)) {
            Map<String, Object> data = new HashMap<>();
            data.put("dailyFlips", dailyFlips);
            data.put("dailyProfits", dailyProfits);
            data.put("itemFlipCounts", itemFlipCounts);
            data.put("totalProfit", totalProfit);
            data.put("totalFlips", totalFlips);
            data.put("firstFlipDate", firstFlipDate != null ? DATE_FORMAT.format(firstFlipDate) : null);
            data.put("lastFlipDate", lastFlipDate != null ? DATE_FORMAT.format(lastFlipDate) : null);
            
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {
        if (!statisticsFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(statisticsFile)) {
            Map<String, Object> data = GSON.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
            
            // Konvertiere die JSON-Elemente zurück in starke Typen
            if (data.containsKey("dailyFlips")) {
                this.dailyFlips = (Map<String, List<FlipRecord>>) data.get("dailyFlips");
            }
            
            if (data.containsKey("dailyProfits")) {
                this.dailyProfits = (Map<String, Double>) data.get("dailyProfits");
            }
            
            if (data.containsKey("itemFlipCounts")) {
                this.itemFlipCounts = (Map<String, Integer>) data.get("itemFlipCounts");
            }
            
            if (data.containsKey("totalProfit")) {
                this.totalProfit = ((Number) data.get("totalProfit")).doubleValue();
            }
            
            if (data.containsKey("totalFlips")) {
                this.totalFlips = ((Number) data.get("totalFlips")).intValue();
            }
            
            if (data.containsKey("firstFlipDate") && data.get("firstFlipDate") != null) {
                try {
                    this.firstFlipDate = DATE_FORMAT.parse((String) data.get("firstFlipDate"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            if (data.containsKey("lastFlipDate") && data.get("lastFlipDate") != null) {
                try {
                    this.lastFlipDate = DATE_FORMAT.parse((String) data.get("lastFlipDate"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter-Methoden für verschiedene Statistiken
    
    public Map<String, Double> getDailyProfits() {
        return dailyProfits;
    }
    
    public double getTotalProfit() {
        return totalProfit;
    }
    
    public int getTotalFlips() {
        return totalFlips;
    }
    
    public Date getFirstFlipDate() {
        return firstFlipDate;
    }
    
    public Date getLastFlipDate() {
        return lastFlipDate;
    }
    
    public Map<String, Integer> getItemFlipCounts() {
        return itemFlipCounts;
    }
    
    public double getAverageProfit() {
        if (totalFlips == 0) return 0;
        return totalProfit / totalFlips;
    }
    
    public Map<String, List<FlipRecord>> getDailyFlips() {
        return dailyFlips;
    }
    
    // Zusätzliche nützliche Methoden
    
    public List<Map.Entry<String, Integer>> getMostFlippedItems(int limit) {
        List<Map.Entry<String, Integer>> items = new ArrayList<>(itemFlipCounts.entrySet());
        items.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return items.subList(0, Math.min(limit, items.size()));
    }
    
    public List<Map.Entry<String, Double>> getMostProfitableDays(int limit) {
        List<Map.Entry<String, Double>> days = new ArrayList<>(dailyProfits.entrySet());
        days.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        return days.subList(0, Math.min(limit, days.size()));
    }
    
    // Innere Klasse zur Aufzeichnung einzelner Flips
    public static class FlipRecord {
        private final String itemId;
        private final String itemName;
        private final double buyPrice;
        private final double sellPrice;
        private final int quantity;
        private final double totalCost;
        private final double totalProfit;
        private final Date timestamp;
        
        public FlipRecord(String itemId, String itemName, double buyPrice, double sellPrice, 
                          int quantity, double totalCost, double totalProfit) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.quantity = quantity;
            this.totalCost = totalCost;
            this.totalProfit = totalProfit;
            this.timestamp = new Date();
        }
        
        public String getItemId() {
            return itemId;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public double getBuyPrice() {
            return buyPrice;
        }
        
        public double getSellPrice() {
            return sellPrice;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public double getTotalCost() {
            return totalCost;
        }
        
        public double getTotalProfit() {
            return totalProfit;
        }
        
        public Date getTimestamp() {
            return timestamp;
        }
    }
}