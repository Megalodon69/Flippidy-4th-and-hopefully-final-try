package com.flippidy.skyblock.handlers;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.models.BazaarItem;
import com.flippidy.skyblock.models.FlipCandidate;
import com.flippidy.skyblock.utils.ApiUtils;
import com.flippidy.skyblock.utils.MessageUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BazaarDataHandler {
    private final ExecutorService executorService;
    private Map<String, BazaarItem> bazaarItems;
    private Map<String, Double> historicalPrices;
    private List<FlipCandidate> flipCandidates;
    private long lastUpdate;
    private boolean isRefreshing;

    public BazaarDataHandler() {
        this.executorService = Executors.newCachedThreadPool();
        this.bazaarItems = new HashMap<>();
        this.historicalPrices = new HashMap<>();
        this.flipCandidates = new ArrayList<>();
        this.lastUpdate = 0;
        this.isRefreshing = false;
    }

    public CompletableFuture<Void> refreshBazaarData() {
        if (isRefreshing) {
            MessageUtils.sendMessage("§cDaten werden bereits aktualisiert, bitte warten...");
            return CompletableFuture.completedFuture(null);
        }

        isRefreshing = true;
        return CompletableFuture.runAsync(() -> {
            try {
                MessageUtils.sendDebugMessage("Hole Basar-Daten von Hypixel API...");
                JsonObject bazaarData = ApiUtils.fetchHypixelBazaarData();
                
                if (bazaarData == null || !bazaarData.has("products")) {
                    MessageUtils.sendMessage("§cFehler beim Abrufen der Basar-Daten!");
                    isRefreshing = false;
                    return;
                }

                JsonObject products = bazaarData.getAsJsonObject("products");
                Map<String, BazaarItem> newBazaarItems = new HashMap<>();

                for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
                    String productId = entry.getKey();
                    JsonObject product = entry.getValue().getAsJsonObject();
                    
                    // Extrahiere Buy-Summary (Verkaufsangebote)
                    double bestSellPrice = 0;
                    int sellVolume = 0;
                    if (product.has("sell_summary") && product.getAsJsonArray("sell_summary").size() > 0) {
                        JsonObject bestSellOrder = product.getAsJsonArray("sell_summary").get(0).getAsJsonObject();
                        bestSellPrice = bestSellOrder.get("pricePerUnit").getAsDouble();
                        sellVolume = bestSellOrder.get("amount").getAsInt();
                    }

                    // Extrahiere Sell-Summary (Kaufangebote)
                    double bestBuyPrice = 0;
                    int buyVolume = 0;
                    if (product.has("buy_summary") && product.getAsJsonArray("buy_summary").size() > 0) {
                        JsonObject bestBuyOrder = product.getAsJsonArray("buy_summary").get(0).getAsJsonObject();
                        bestBuyPrice = bestBuyOrder.get("pricePerUnit").getAsDouble();
                        buyVolume = bestBuyOrder.get("amount").getAsInt();
                    }

                    // Erstelle BazaarItem
                    BazaarItem item = new BazaarItem(
                            productId,
                            product.get("product_id").getAsString(),
                            bestBuyPrice,
                            bestSellPrice,
                            buyVolume,
                            sellVolume
                    );
                    
                    newBazaarItems.put(productId, item);
                }

                // Update bazaarItems
                this.bazaarItems = newBazaarItems;
                this.lastUpdate = System.currentTimeMillis();
                
                // Hole historische Preisdaten
                fetchHistoricalPrices();
                
                MessageUtils.sendDebugMessage("Basar-Daten aktualisiert! " + bazaarItems.size() + " Items gefunden.");
            } catch (Exception e) {
                MessageUtils.sendMessage("§cFehler beim Aktualisieren der Basar-Daten: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isRefreshing = false;
            }
        }, executorService);
    }

    private void fetchHistoricalPrices() {
        try {
            MessageUtils.sendDebugMessage("Hole historische Preisdaten von CoflNet API...");
            JsonObject historicalData = ApiUtils.fetchCoflnetHistoricalData();
            
            if (historicalData == null) {
                MessageUtils.sendMessage("§cFehler beim Abrufen der historischen Daten!");
                return;
            }

            Map<String, Double> newHistoricalPrices = new HashMap<>();
            
            // Extrahiere monatliche Durchschnittspreise für jeden Gegenstand
            for (Map.Entry<String, JsonElement> entry : historicalData.entrySet()) {
                String itemId = entry.getKey();
                Double avgPrice = entry.getValue().getAsJsonObject().get("monthlyAvgPrice").getAsDouble();
                newHistoricalPrices.put(itemId, avgPrice);
            }

            this.historicalPrices = newHistoricalPrices;
            MessageUtils.sendDebugMessage("Historische Preisdaten aktualisiert! " + historicalPrices.size() + " Einträge gefunden.");
        } catch (Exception e) {
            MessageUtils.sendMessage("§cFehler beim Abrufen der historischen Daten: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> calculateFlips(double purseCoins) {
        if (bazaarItems.isEmpty()) {
            MessageUtils.sendMessage("§cKeine Basar-Daten verfügbar. Bitte aktualisieren Sie die Daten zuerst!");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                MessageUtils.sendDebugMessage("Berechne profitable Flips...");
                List<FlipCandidate> candidates = new ArrayList<>();

                for (BazaarItem item : bazaarItems.values()) {
                    double buyPrice = item.getBestSellPrice(); // Preis, zu dem wir kaufen
                    double sellPrice = item.getBestBuyPrice(); // Preis, zu dem wir verkaufen
                    double spread = sellPrice - buyPrice;
                    
                    // Berechne Gewinn nach Gebühren (0,5% pro Transaktion)
                    double profit = (sellPrice * 0.995) - (buyPrice * 1.005);
                    double profitMargin = profit / buyPrice * 100;
                    
                    // Prüfe auf Manipulation
                    boolean isSuspicious = checkForManipulation(item);
                    
                    // Prüfe, ob die Volumen ausreichend sind
                    boolean hasEnoughVolume = item.getBuyVolume() >= 10 && item.getSellVolume() >= 10;
                    
                    // Erstelle einen FlipCandidate, wenn profitabel und nicht manipuliert
                    if (profit > 0 && profitMargin >= 1.0 && !isSuspicious && hasEnoughVolume && buyPrice <= purseCoins) {
                        // Wie viele Items können wir mit den verfügbaren Coins kaufen?
                        int maxItems = (int) Math.min(Math.floor(purseCoins / buyPrice), item.getSellVolume());
                        // Begrenzen Sie es auch auf die Anzahl, die wir verkaufen können
                        maxItems = Math.min(maxItems, item.getBuyVolume());
                        
                        double totalInvestment = maxItems * buyPrice;
                        double totalProfit = maxItems * profit;
                        
                        FlipCandidate candidate = new FlipCandidate(
                                item,
                                profit,
                                profitMargin,
                                maxItems,
                                totalInvestment,
                                totalProfit
                        );
                        
                        candidates.add(candidate);
                    }
                }

                // Sortiere Kandidaten nach Gesamtgewinn
                candidates.sort(Comparator.comparing(FlipCandidate::getTotalProfit).reversed());
                
                // Behalte nur die Top-Kandidaten
                this.flipCandidates = candidates.subList(0, Math.min(10, candidates.size()));
                
                MessageUtils.sendDebugMessage("Flip-Berechnung abgeschlossen. " + flipCandidates.size() + " profitable Flips gefunden.");
            } catch (Exception e) {
                MessageUtils.sendMessage("§cFehler bei der Berechnung von Flips: " + e.getMessage());
                e.printStackTrace();
            }
        }, executorService);
    }

    private boolean checkForManipulation(BazaarItem item) {
        // 1. Prüfe auf ungewöhnlich hohe Preisdifferenzen
        double spread = item.getBestBuyPrice() - item.getBestSellPrice();
        double spreadPercentage = spread / item.getBestSellPrice() * 100;
        
        if (spreadPercentage > 50) {
            return true; // Ungewöhnlich hohe Preisdifferenz
        }
        
        // 2. Vergleiche mit historischen Daten
        Double historicalPrice = historicalPrices.get(item.getProductId());
        if (historicalPrice != null) {
            double currentAvg = (item.getBestBuyPrice() + item.getBestSellPrice()) / 2;
            double deviation = Math.abs(currentAvg - historicalPrice) / historicalPrice * 100;
            
            if (deviation > 30) {
                return true; // Preisabweichung von mehr als 30% vom historischen Durchschnitt
            }
        }
        
        // 3. Prüfe auf zu niedrige Volumen
        if (item.getBuyVolume() < 5 || item.getSellVolume() < 5) {
            return true; // Zu niedrige Volumen können auf Manipulation hindeuten
        }
        
        return false;
    }

    public List<FlipCandidate> getFlipCandidates() {
        return flipCandidates;
    }

    public Map<String, BazaarItem> getBazaarItems() {
        return bazaarItems;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}