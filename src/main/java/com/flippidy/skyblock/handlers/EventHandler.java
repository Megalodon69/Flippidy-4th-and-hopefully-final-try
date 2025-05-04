package com.flippidy.skyblock.handlers;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.models.BazaarItem;
import com.flippidy.skyblock.models.UserProfile;
import com.flippidy.skyblock.utils.ApiUtils;
import com.flippidy.skyblock.utils.MessageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final File eventsFile;
    private List<SkyblockEvent> upcomingEvents;
    private List<SkyblockEvent> activeEvents;
    private List<EventInvestment> activeInvestments;
    private Timer eventCheckTimer;
    
    public EventHandler(File configDir) {
        this.eventsFile = new File(configDir, "events.json");
        this.upcomingEvents = new ArrayList<>();
        this.activeEvents = new ArrayList<>();
        this.activeInvestments = new ArrayList<>();
        
        loadEvents();
        startEventTimer();
    }
    
    private void loadEvents() {
        if (!eventsFile.exists()) {
            // Wenn keine Event-Datei existiert, füge einige Standardevents hinzu
            initializeDefaultEvents();
            saveEvents();
            return;
        }
        
        try (FileReader reader = new FileReader(eventsFile)) {
            Map<String, Object> data = GSON.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
            
            // Lade die Events
            if (data.containsKey("upcomingEvents")) {
                upcomingEvents = GSON.fromJson(GSON.toJson(data.get("upcomingEvents")), 
                        new TypeToken<List<SkyblockEvent>>(){}.getType());
            }
            
            if (data.containsKey("activeEvents")) {
                activeEvents = GSON.fromJson(GSON.toJson(data.get("activeEvents")), 
                        new TypeToken<List<SkyblockEvent>>(){}.getType());
            }
            
            if (data.containsKey("activeInvestments")) {
                activeInvestments = GSON.fromJson(GSON.toJson(data.get("activeInvestments")), 
                        new TypeToken<List<EventInvestment>>(){}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Bei Fehler, initialisiere mit Standardwerten
            initializeDefaultEvents();
        }
    }
    
    private void initializeDefaultEvents() {
        upcomingEvents = new ArrayList<>();
        activeEvents = new ArrayList<>();
        activeInvestments = new ArrayList<>();
        
        // Füge einige typische Skyblock-Events hinzu
        Calendar cal = Calendar.getInstance();
        
        // Mayor Election (alle 5 Tage)
        cal.add(Calendar.DAY_OF_MONTH, 3);
        upcomingEvents.add(new SkyblockEvent(
                "Mayor Election",
                "Zeit für einen neuen Mayor! Preise für bestimmte Items können stark schwanken.",
                cal.getTime(),
                SkyblockEvent.EventType.MAYOR_ELECTION,
                Arrays.asList("ENCHANTED_QUARTZ", "ENCHANTED_REDSTONE", "STRONG_FRAGMENT")
        ));
        
        // Dark Auction (wiederkehrend)
        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 4);
        upcomingEvents.add(new SkyblockEvent(
                "Dark Auction",
                "Preise für Dungeon-Items und seltene Materialien können steigen.",
                cal.getTime(),
                SkyblockEvent.EventType.RECURRING,
                Arrays.asList("DARK_ORB", "ENCHANTED_GOLD", "ENCHANTED_GHAST_TEAR")
        ));
        
        // Spooky Festival (saisonal)
        cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        upcomingEvents.add(new SkyblockEvent(
                "Spooky Festival",
                "Halloween-Event mit speziellen Items und hoher Nachfrage nach spezifischen Materialien.",
                cal.getTime(),
                SkyblockEvent.EventType.SEASONAL,
                Arrays.asList("ENCHANTED_PUMPKIN", "ENCHANTED_BONE", "ENCHANTED_EYE_OF_ENDER")
        ));
    }
    
    private void saveEvents() {
        try (FileWriter writer = new FileWriter(eventsFile)) {
            Map<String, Object> data = new HashMap<>();
            data.put("upcomingEvents", upcomingEvents);
            data.put("activeEvents", activeEvents);
            data.put("activeInvestments", activeInvestments);
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void startEventTimer() {
        // Stoppe den Timer, falls er bereits läuft
        if (eventCheckTimer != null) {
            eventCheckTimer.cancel();
        }
        
        // Starte einen neuen Timer, der alle 5 Minuten prüft, ob ein Event begonnen hat
        eventCheckTimer = new Timer("EventCheckTimer");
        eventCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkEvents();
            }
        }, 0, 5 * 60 * 1000); // 5 Minuten
    }
    
    private void checkEvents() {
        Date now = new Date();
        List<SkyblockEvent> newActiveEvents = new ArrayList<>();
        List<SkyblockEvent> stillUpcomingEvents = new ArrayList<>();
        boolean hasChanges = false;
        
        // Prüfe, ob neue Events begonnen haben
        for (SkyblockEvent event : upcomingEvents) {
            if (event.getStartTime().before(now)) {
                // Event hat begonnen
                newActiveEvents.add(event);
                hasChanges = true;
                
                // Benachrichtige den Benutzer
                MessageUtils.sendMessage("§e§l[Event Start] §r§6" + event.getName() + " §rhat begonnen!");
                suggestInvestments(event);
            } else {
                // Event ist noch nicht gestartet
                stillUpcomingEvents.add(event);
            }
        }
        
        // Aktualisiere die Listen, wenn sich etwas geändert hat
        if (hasChanges) {
            activeEvents.addAll(newActiveEvents);
            upcomingEvents = stillUpcomingEvents;
            saveEvents();
        }
    }
    
    public void suggestInvestments(SkyblockEvent event) {
        if (event == null || event.getAffectedItems().isEmpty()) {
            return;
        }
        
        // Hole Bazar-Daten, um aktuelle Preise zu bekommen
        FlippidyMod mod = FlippidyMod.getInstance();
        Map<String, BazaarItem> bazaarItems = mod.getBazaarDataHandler().getBazaarItems();
        
        if (bazaarItems == null || bazaarItems.isEmpty()) {
            // Versuche, die Daten zu aktualisieren, wenn sie fehlen
            mod.getBazaarDataHandler().refreshBazaarData().thenRun(() -> {
                bazaarItems = mod.getBazaarDataHandler().getBazaarItems();
                if (!bazaarItems.isEmpty()) {
                    doSuggestInvestments(event, bazaarItems);
                }
            });
        } else {
            doSuggestInvestments(event, bazaarItems);
        }
    }
    
    private void doSuggestInvestments(SkyblockEvent event, Map<String, BazaarItem> bazaarItems) {
        List<InvestmentSuggestion> suggestions = new ArrayList<>();
        
        for (String itemId : event.getAffectedItems()) {
            BazaarItem item = bazaarItems.get(itemId);
            if (item == null) continue;
            
            // Berechne potenzielle Rendite basierend auf Eventtyp
            double potentialReturn = calculatePotentialReturn(event.getType(), item);
            
            if (potentialReturn > 5.0) { // Nur Vorschläge mit mindestens 5% potenziellem Gewinn
                suggestions.add(new InvestmentSuggestion(
                        item,
                        potentialReturn,
                        event.getType() == SkyblockEvent.EventType.MAYOR_ELECTION ? 70.0 : 30.0, // Investitionsanteil
                        "Event: " + event.getName()
                ));
            }
        }
        
        // Sortiere nach potenzieller Rendite
        suggestions.sort(Comparator.comparing(InvestmentSuggestion::getPotentialReturn).reversed());
        
        // Zeige Vorschläge an
        if (!suggestions.isEmpty()) {
            MessageUtils.sendMessage("§6§l--- Investitionsvorschläge für " + event.getName() + " ---");
            
            for (int i = 0; i < Math.min(3, suggestions.size()); i++) {
                InvestmentSuggestion suggestion = suggestions.get(i);
                MessageUtils.sendMessage(
                        "§6" + (i+1) + ". §b" + formatItemName(suggestion.getItem().getProductId()) + 
                        " §7- Kaufen: §a" + formatNumber(suggestion.getItem().getBestSellPrice()) + 
                        " §7Potenzielle Rendite: §a" + formatNumber(suggestion.getPotentialReturn()) + "%" +
                        " §7Empfohlene Investition: §a" + formatNumber(suggestion.getRecommendedPercentage()) + "% des Vermögens"
                );
            }
        }
    }
    
    private double calculatePotentialReturn(SkyblockEvent.EventType eventType, BazaarItem item) {
        // Historische Daten würden hier idealerweise verwendet werden
        // Für jetzt verwenden wir eine einfache Schätzung basierend auf Eventtyp
        
        switch (eventType) {
            case MAYOR_ELECTION:
                // Mayor-Wahlen können zu erheblichen Preisschwankungen führen
                return 15.0 + (Math.random() * 10.0);
            case SEASONAL:
                // Saisonale Events haben oft vorhersehbare Preisänderungen
                return 10.0 + (Math.random() * 15.0);
            case RECURRING:
                // Wiederkehrende Events haben typischerweise kleinere Schwankungen
                return 5.0 + (Math.random() * 10.0);
            default:
                return 0.0;
        }
    }
    
    public void investInItem(BazaarItem item, double percentageOfPurse) {
        FlippidyMod mod = FlippidyMod.getInstance();
        UserProfile profile = mod.getProfileManager().getActiveProfile();
        
        // Prüfe, ob Profil konfiguriert ist
        if (profile == null) {
            MessageUtils.sendMessage("§cKein aktives Profil gefunden!");
            return;
        }
        
        // Stelle sicher, dass der Prozentsatz innerhalb der konfigurierten Grenzen liegt
        double maxPercent = profile.getMaxInvestmentPercentage();
        for (SkyblockEvent event : activeEvents) {
            if (event.getAffectedItems().contains(item.getProductId())) {
                // Falls es ein aktives Event ist, verwende den höheren Prozentsatz
                maxPercent = profile.getMaxEventInvestmentPercentage();
                break;
            }
        }
        
        // Begrenze auf den konfigurierten Maximalwert
        double investmentPercentage = Math.min(percentageOfPurse, maxPercent);
        
        // Hole den aktuellen Spieler-Geldbeutel
        double purseCoins = ApiUtils.fetchPlayerPurse();
        
        // Berechne den Investitionsbetrag
        double investmentAmount = purseCoins * (investmentPercentage / 100.0);
        
        // Berechne wie viele Items gekauft werden können
        int itemCount = (int) Math.floor(investmentAmount / item.getBestSellPrice());
        
        if (itemCount <= 0) {
            MessageUtils.sendMessage("§cNicht genug Münzen für diese Investition!");
            return;
        }
        
        // Füge zur Liste der aktiven Investitionen hinzu
        activeInvestments.add(new EventInvestment(
                item.getProductId(),
                formatItemName(item.getProductId()),
                item.getBestSellPrice(),
                itemCount,
                new Date()
        ));
        
        saveEvents();
        
        // Gib Feedback
        MessageUtils.sendMessage(
                "§aInvestition getätigt: §b" + itemCount + "x " + formatItemName(item.getProductId()) + 
                " §afür insgesamt §e" + formatNumber(itemCount * item.getBestSellPrice()) + " Münzen" +
                " §a(" + formatNumber(investmentPercentage) + "% des Vermögens)"
        );
    }
    
    public List<SkyblockEvent> getUpcomingEvents() {
        return upcomingEvents;
    }
    
    public List<SkyblockEvent> getActiveEvents() {
        return activeEvents;
    }
    
    public List<EventInvestment> getActiveInvestments() {
        return activeInvestments;
    }
    
    public void shutdown() {
        if (eventCheckTimer != null) {
            eventCheckTimer.cancel();
        }
    }
    
    private String formatItemName(String productId) {
        // Konvertiere IDs wie "ENCHANTED_CARROT" zu "Enchanted Carrot"
        String[] parts = productId.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(part.charAt(0))
                      .append(part.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    private String formatNumber(double number) {
        return String.format("%,.2f", number);
    }
    
    // Innere Klassen für Events und Investitionen
    
    public static class SkyblockEvent {
        private String name;
        private String description;
        private Date startTime;
        private EventType type;
        private List<String> affectedItems;
        
        public SkyblockEvent(String name, String description, Date startTime, EventType type, List<String> affectedItems) {
            this.name = name;
            this.description = description;
            this.startTime = startTime;
            this.type = type;
            this.affectedItems = affectedItems;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Date getStartTime() {
            return startTime;
        }
        
        public EventType getType() {
            return type;
        }
        
        public List<String> getAffectedItems() {
            return affectedItems;
        }
        
        public enum EventType {
            MAYOR_ELECTION,
            SEASONAL,
            RECURRING
        }
    }
    
    public static class EventInvestment {
        private String itemId;
        private String itemName;
        private double buyPrice;
        private int quantity;
        private Date purchaseDate;
        
        public EventInvestment(String itemId, String itemName, double buyPrice, int quantity, Date purchaseDate) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.buyPrice = buyPrice;
            this.quantity = quantity;
            this.purchaseDate = purchaseDate;
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
        
        public int getQuantity() {
            return quantity;
        }
        
        public Date getPurchaseDate() {
            return purchaseDate;
        }
        
        public double getTotalInvestment() {
            return buyPrice * quantity;
        }
    }
    
    public static class InvestmentSuggestion {
        private BazaarItem item;
        private double potentialReturn;
        private double recommendedPercentage;
        private String reason;
        
        public InvestmentSuggestion(BazaarItem item, double potentialReturn, double recommendedPercentage, String reason) {
            this.item = item;
            this.potentialReturn = potentialReturn;
            this.recommendedPercentage = recommendedPercentage;
            this.reason = reason;
        }
        
        public BazaarItem getItem() {
            return item;
        }
        
        public double getPotentialReturn() {
            return potentialReturn;
        }
        
        public double getRecommendedPercentage() {
            return recommendedPercentage;
        }
        
        public String getReason() {
            return reason;
        }
    }
}