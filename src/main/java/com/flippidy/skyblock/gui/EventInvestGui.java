package com.flippidy.skyblock.gui;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.handlers.EventHandler;
import com.flippidy.skyblock.models.BazaarItem;
import com.flippidy.skyblock.models.UserProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventInvestGui extends GuiScreen {
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    private final EventHandler.SkyblockEvent event;
    private final FlippidyMod mod;
    private final UserProfile profile;
    private List<EventHandler.InvestmentSuggestion> suggestions;
    private GuiButton backButton;
    private GuiButton investButton;
    private GuiTextField percentageField;
    private int selectedSuggestionIndex = -1;
    private int scrollOffset = 0;
    
    public EventInvestGui(EventHandler.SkyblockEvent event) {
        this.event = event;
        this.mod = FlippidyMod.getInstance();
        this.profile = mod.getProfileManager().getActiveProfile();
        this.suggestions = new ArrayList<>();
        
        // Generiere Investitionsvorschläge auf Basis der Event-Items
        generateSuggestions();
    }
    
    private void generateSuggestions() {
        List<EventHandler.InvestmentSuggestion> tempSuggestions = new ArrayList<>();
        
        for (String itemId : event.getAffectedItems()) {
            BazaarItem item = mod.getBazaarDataHandler().getBazaarItems().get(itemId);
            if (item == null) continue;
            
            // Berechne potenzielle Rendite
            double potentialReturn = calculatePotentialReturn(event.getType(), item);
            
            double recommendedPercentage = event.getType() == EventHandler.SkyblockEvent.EventType.MAYOR_ELECTION ? 
                    Math.min(70.0, profile.getMaxEventInvestmentPercentage()) : 
                    Math.min(30.0, profile.getMaxInvestmentPercentage());
            
            tempSuggestions.add(new EventHandler.InvestmentSuggestion(
                    item, 
                    potentialReturn, 
                    recommendedPercentage, 
                    "Event: " + event.getName()
            ));
        }
        
        // Sortiere nach potenzieller Rendite
        tempSuggestions.sort(Comparator.comparing(EventHandler.InvestmentSuggestion::getPotentialReturn).reversed());
        suggestions = tempSuggestions;
    }
    
    private double calculatePotentialReturn(EventHandler.SkyblockEvent.EventType eventType, BazaarItem item) {
        // Verwende die gleiche Logik wie im EventHandler
        switch (eventType) {
            case MAYOR_ELECTION:
                return 15.0 + (Math.random() * 10.0);
            case SEASONAL:
                return 10.0 + (Math.random() * 15.0);
            case RECURRING:
                return 5.0 + (Math.random() * 10.0);
            default:
                return 0.0;
        }
    }
    
    @Override
    public void initGui() {
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        int buttonWidth = 120;
        int buttonY = height - BUTTON_HEIGHT - PADDING;
        
        // Erstelle Buttons
        backButton = new GuiButton(1, width - buttonWidth - PADDING, buttonY, buttonWidth, BUTTON_HEIGHT, "Zurück");
        buttonList.add(backButton);
        
        investButton = new GuiButton(2, (width - buttonWidth) / 2, buttonY, buttonWidth, BUTTON_HEIGHT, "Investieren");
        investButton.enabled = false;
        buttonList.add(investButton);
        
        // Textfeld für Investitionsprozentsatz
        percentageField = new GuiTextField(3, fontRendererObj, PADDING, buttonY, 80, BUTTON_HEIGHT);
        percentageField.setMaxStringLength(5);
        percentageField.setText(event.getType() == EventHandler.SkyblockEvent.EventType.MAYOR_ELECTION ? "70.0" : "30.0");
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Zeichne Hintergrund
        drawDefaultBackground();
        
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        // Zeichne Überschrift
        String title = EnumChatFormatting.GOLD + "Investieren in: " + EnumChatFormatting.AQUA + event.getName();
        fontRendererObj.drawStringWithShadow(title, (width - fontRendererObj.getStringWidth(title)) / 2, PADDING, 0xFFFFFF);
        
        // Zeichne Event-Beschreibung
        String desc = EnumChatFormatting.GRAY + event.getDescription();
        fontRendererObj.drawStringWithShadow(desc, (width - fontRendererObj.getStringWidth(desc)) / 2, PADDING + fontRendererObj.FONT_HEIGHT + 5, 0xFFFFFF);
        
        // Zeichne Event-Typ
        String typeText = EnumChatFormatting.YELLOW + "Typ: ";
        switch (event.getType()) {
            case MAYOR_ELECTION:
                typeText += EnumChatFormatting.GOLD + "Mayor-Wahl";
                break;
            case SEASONAL:
                typeText += EnumChatFormatting.GREEN + "Saisonal";
                break;
            case RECURRING:
                typeText += EnumChatFormatting.AQUA + "Wiederkehrend";
                break;
        }
        fontRendererObj.drawStringWithShadow(typeText, (width - fontRendererObj.getStringWidth(typeText)) / 2, 
                PADDING + fontRendererObj.FONT_HEIGHT * 2 + 10, 0xFFFFFF);
        
        // Zeichne Investitionsvorschläge
        int tableTop = PADDING + fontRendererObj.FONT_HEIGHT * 3 + 20;
        int tableBottom = height - PADDING * 2 - BUTTON_HEIGHT;
        
        // Tabellen-Header
        drawRect(PADDING, tableTop, width - PADDING, tableTop + 20, new Color(0, 0, 0, 200).getRGB());
        String[] headers = {"Item", "Kaufpreis", "Potenzielle Rendite", "Empfohlene Investition"};
        int[] columnWidths = {width / 2 - PADDING, width / 6, width / 6, width / 6};
        
        int headerX = PADDING + 5;
        for (int i = 0; i < headers.length; i++) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + headers[i], headerX, tableTop + 6, 0xFFFFFF);
            headerX += columnWidths[i];
        }
        
        // Tabelle mit Vorschlägen
        int suggestionTop = tableTop + 20;
        int rowHeight = fontRendererObj.FONT_HEIGHT + 8;
        int visibleRows = (tableBottom - suggestionTop) / rowHeight;
        
        if (suggestions.isEmpty()) {
            String noDataText = EnumChatFormatting.GRAY + "Keine Investitionsvorschläge für dieses Event verfügbar.";
            fontRendererObj.drawStringWithShadow(noDataText, 
                    (width - fontRendererObj.getStringWidth(noDataText)) / 2, 
                    suggestionTop + rowHeight * 2, 0xFFFFFF);
        } else {
            for (int i = 0; i < Math.min(visibleRows, suggestions.size()); i++) {
                EventHandler.InvestmentSuggestion suggestion = suggestions.get(i + scrollOffset);
                int rowY = suggestionTop + i * rowHeight;
                
                // Hintergrund (hervorheben wenn ausgewählt)
                boolean isSelected = (i + scrollOffset == selectedSuggestionIndex);
                drawRect(PADDING, rowY, width - PADDING, rowY + rowHeight, 
                        isSelected ? new Color(60, 100, 60, 180).getRGB() : 
                        ((i % 2 == 0) ? new Color(40, 40, 40, 180).getRGB() : new Color(50, 50, 50, 180).getRGB()));
                
                // Zeilendaten
                int colX = PADDING + 5;
                
                // Item-Name
                fontRendererObj.drawStringWithShadow(EnumChatFormatting.AQUA + formatItemName(suggestion.getItem().getProductId()), 
                        colX, rowY + 4, 0xFFFFFF);
                colX += columnWidths[0];
                
                // Kaufpreis
                fontRendererObj.drawStringWithShadow(EnumChatFormatting.GREEN + df.format(suggestion.getItem().getBestSellPrice()), 
                        colX, rowY + 4, 0xFFFFFF);
                colX += columnWidths[1];
                
                // Potenzielle Rendite
                fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + df.format(suggestion.getPotentialReturn()) + "%", 
                        colX, rowY + 4, 0xFFFFFF);
                colX += columnWidths[2];
                
                // Empfohlene Investition
                fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + df.format(suggestion.getRecommendedPercentage()) + "% des Vermögens", 
                        colX, rowY + 4, 0xFFFFFF);
            }
        }
        
        // Zeichne Prozentsatz-Label
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + "Investitionsprozentsatz:", 
                PADDING, height - BUTTON_HEIGHT - PADDING - fontRendererObj.FONT_HEIGHT - 5, 0xFFFFFF);
        
        // Zeichne Textfeld
        percentageField.drawTextBox();
        
        // Zeichne Buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        
        // Erlaube nur Zahlen und Punkt im Prozentsatz-Feld
        if (percentageField.isFocused() && 
                (Character.isDigit(typedChar) || typedChar == '.' || typedChar == '\b' || keyCode == 203 || keyCode == 205)) {
            percentageField.textboxKeyTyped(typedChar, keyCode);
            
            // Aktiviere den Investieren-Button, wenn ein gültiger Prozentsatz eingegeben wurde
            try {
                double percentage = Double.parseDouble(percentageField.getText());
                investButton.enabled = percentage > 0 && percentage <= 100 && selectedSuggestionIndex >= 0;
            } catch (NumberFormatException e) {
                investButton.enabled = false;
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        percentageField.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Prüfe Klicks auf Vorschläge
        if (mouseButton == 0 && mouseX >= PADDING && mouseX <= width - PADDING) {
            int tableTop = PADDING + fontRendererObj.FONT_HEIGHT * 3 + 20;
            int suggestionTop = tableTop + 20;
            int rowHeight = fontRendererObj.FONT_HEIGHT + 8;
            
            if (mouseY >= suggestionTop && mouseY < height - PADDING * 2 - BUTTON_HEIGHT) {
                int clickedRow = (mouseY - suggestionTop) / rowHeight;
                
                if (clickedRow >= 0 && clickedRow + scrollOffset < suggestions.size()) {
                    selectedSuggestionIndex = clickedRow + scrollOffset;
                    
                    // Aktiviere den Investieren-Button, wenn ein gültiger Prozentsatz eingegeben wurde
                    try {
                        double percentage = Double.parseDouble(percentageField.getText());
                        investButton.enabled = percentage > 0 && percentage <= 100;
                    } catch (NumberFormatException e) {
                        // Bei ungültigem Prozentsatz setze den empfohlenen Wert
                        EventHandler.InvestmentSuggestion suggestion = suggestions.get(selectedSuggestionIndex);
                        percentageField.setText(String.valueOf(suggestion.getRecommendedPercentage()));
                        investButton.enabled = true;
                    }
                }
            }
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Back
                Minecraft.getMinecraft().displayGuiScreen(new EventsGui());
                break;
                
            case 2: // Invest
                if (selectedSuggestionIndex >= 0 && selectedSuggestionIndex < suggestions.size()) {
                    try {
                        double percentage = Double.parseDouble(percentageField.getText());
                        
                        // Begrenze auf konfigurierte Maximalwerte
                        double maxAllowed = event.getType() == EventHandler.SkyblockEvent.EventType.MAYOR_ELECTION ? 
                                profile.getMaxEventInvestmentPercentage() : profile.getMaxInvestmentPercentage();
                        percentage = Math.min(percentage, maxAllowed);
                        
                        // Führe Investition durch
                        EventHandler.InvestmentSuggestion suggestion = suggestions.get(selectedSuggestionIndex);
                        mod.getEventHandler().investInItem(suggestion.getItem(), percentage);
                        
                        // Zurück zur Event-Übersicht
                        Minecraft.getMinecraft().displayGuiScreen(new EventsGui());
                    } catch (NumberFormatException e) {
                        // Ignoriere ungültige Eingaben
                    }
                }
                break;
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int mouseScroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (mouseScroll != 0) {
            scrollOffset += (mouseScroll > 0) ? -1 : 1;
            scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, suggestions.size() - 1)));
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
}