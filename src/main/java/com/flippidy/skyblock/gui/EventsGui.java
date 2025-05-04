package com.flippidy.skyblock.gui;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.handlers.EventHandler;
import com.flippidy.skyblock.models.BazaarItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class EventsGui extends GuiScreen {
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    
    private FlippidyMod mod;
    private EventHandler eventHandler;
    private GuiButton backButton;
    private GuiButton refreshButton;
    private GuiButton investButton;
    private int selectedEventIndex = -1;
    private int selectedInvestmentIndex = -1;
    private int scrollOffset = 0;
    
    public EventsGui() {
        this.mod = FlippidyMod.getInstance();
        this.eventHandler = mod.getEventHandler();
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
        
        refreshButton = new GuiButton(2, PADDING, buttonY, buttonWidth, BUTTON_HEIGHT, "Aktualisieren");
        buttonList.add(refreshButton);
        
        investButton = new GuiButton(3, (width - buttonWidth) / 2, buttonY, buttonWidth, BUTTON_HEIGHT, "Investieren");
        investButton.enabled = false;
        buttonList.add(investButton);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Zeichne Hintergrund
        drawDefaultBackground();
        
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        // Zeichne Überschrift
        String title = EnumChatFormatting.GOLD + "Flippidy - Events und Investitionen";
        fontRendererObj.drawStringWithShadow(title, (width - fontRendererObj.getStringWidth(title)) / 2, PADDING, 0xFFFFFF);
        
        // Berechne Layout-Dimensionen
        int sectionTop = PADDING * 3 + fontRendererObj.FONT_HEIGHT;
        int sectionHeight = (height - sectionTop - PADDING * 2 - BUTTON_HEIGHT) / 2;
        
        // Zeichne aktive Events Sektion
        drawSectionHeader("Aktive Events", PADDING, sectionTop);
        drawEventList(PADDING, sectionTop + 15, width / 2 - PADDING * 2, sectionHeight - 15, 
                eventHandler.getActiveEvents(), mouseX, mouseY);
        
        // Zeichne bevorstehende Events Sektion
        drawSectionHeader("Bevorstehende Events", PADDING, sectionTop + sectionHeight + PADDING);
        drawEventList(PADDING, sectionTop + sectionHeight + PADDING + 15, width / 2 - PADDING * 2, 
                sectionHeight - 15, eventHandler.getUpcomingEvents(), mouseX, mouseY);
        
        // Zeichne aktive Investitionen Sektion
        drawSectionHeader("Aktive Investitionen", width / 2 + PADDING, sectionTop);
        drawInvestmentList(width / 2 + PADDING, sectionTop + 15, width / 2 - PADDING * 2, 
                height - sectionTop - PADDING * 2 - BUTTON_HEIGHT - 15, mouseX, mouseY);
        
        // Zeichne Buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawSectionHeader(String title, int x, int y) {
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + title, x, y, 0xFFFFFF);
        drawHorizontalLine(x, x + 200, y + fontRendererObj.FONT_HEIGHT, 0xAAAAAA);
    }
    
    private void drawEventList(int x, int y, int width, int height, List<EventHandler.SkyblockEvent> events, int mouseX, int mouseY) {
        // Zeichne Hintergrund
        drawRect(x, y, x + width, y + height, new Color(30, 30, 30, 180).getRGB());
        
        if (events.isEmpty()) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Keine Events verfügbar", 
                    x + 5, y + height / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFF);
            return;
        }
        
        int rowHeight = 45; // Höhe pro Event-Eintrag
        int visibleRows = height / rowHeight;
        
        // Zeichne Events
        for (int i = 0; i < Math.min(visibleRows, events.size()); i++) {
            EventHandler.SkyblockEvent event = events.get(i);
            int rowY = y + i * rowHeight;
            
            // Hintergrund (hervorheben wenn ausgewählt)
            boolean isSelected = (events == eventHandler.getActiveEvents() && i == selectedEventIndex);
            drawRect(x, rowY, x + width, rowY + rowHeight - 1, 
                    isSelected ? new Color(60, 100, 60, 180).getRGB() : new Color(40, 40, 40, 180).getRGB());
            
            // Event-Name
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.AQUA + event.getName(), 
                    x + 5, rowY + 5, 0xFFFFFF);
            
            // Event-Beschreibung (gekürzt)
            String desc = event.getDescription();
            if (desc.length() > 50) desc = desc.substring(0, 47) + "...";
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + desc, 
                    x + 5, rowY + 15, 0xFFFFFF);
            
            // Event-Zeit
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + "Datum: " + 
                    EnumChatFormatting.WHITE + DATE_FORMAT.format(event.getStartTime()), 
                    x + 5, rowY + 25, 0xFFFFFF);
            
            // Event-Typ
            String typeText = "Typ: ";
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
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + typeText, 
                    x + 5, rowY + 35, 0xFFFFFF);
        }
    }
    
    private void drawInvestmentList(int x, int y, int width, int height, int mouseX, int mouseY) {
        // Zeichne Hintergrund
        drawRect(x, y, x + width, y + height, new Color(30, 30, 30, 180).getRGB());
        
        List<EventHandler.EventInvestment> investments = eventHandler.getActiveInvestments();
        
        if (investments.isEmpty()) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Keine aktiven Investitionen", 
                    x + 5, y + height / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFF);
            return;
        }
        
        int rowHeight = 35; // Höhe pro Investment-Eintrag
        int visibleRows = height / rowHeight;
        
        // Zeichne Investitionen
        for (int i = 0; i < Math.min(visibleRows, investments.size()); i++) {
            EventHandler.EventInvestment investment = investments.get(i);
            int rowY = y + i * rowHeight;
            
            // Hintergrund (hervorheben wenn ausgewählt)
            boolean isSelected = (i == selectedInvestmentIndex);
            drawRect(x, rowY, x + width, rowY + rowHeight - 1, 
                    isSelected ? new Color(60, 60, 100, 180).getRGB() : new Color(40, 40, 40, 180).getRGB());
            
            // Item-Name
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.AQUA + investment.getItemName(), 
                    x + 5, rowY + 5, 0xFFFFFF);
            
            // Kaufdetails
            String buyDetails = EnumChatFormatting.YELLOW + "Gekauft: " + 
                    EnumChatFormatting.WHITE + investment.getQuantity() + "x für " + 
                    EnumChatFormatting.GOLD + df.format(investment.getBuyPrice()) + " pro Stück";
            fontRendererObj.drawStringWithShadow(buyDetails, x + 5, rowY + 15, 0xFFFFFF);
            
            // Gesamtinvestition
            String totalInvestment = EnumChatFormatting.YELLOW + "Gesamt: " + 
                    EnumChatFormatting.GOLD + df.format(investment.getTotalInvestment()) + " Münzen";
            fontRendererObj.drawStringWithShadow(totalInvestment, x + 5, rowY + 25, 0xFFFFFF);
            
            // Aktueller Verkaufspreis (falls verfügbar)
            BazaarItem currentItem = mod.getBazaarDataHandler().getBazaarItems().get(investment.getItemId());
            if (currentItem != null) {
                double currentSellPrice = currentItem.getBestBuyPrice();
                double profit = (currentSellPrice - investment.getBuyPrice()) * investment.getQuantity();
                String profitText = EnumChatFormatting.YELLOW + "Aktueller Gewinn: " + 
                        (profit >= 0 ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + 
                        df.format(profit) + " Münzen";
                fontRendererObj.drawStringWithShadow(profitText, x + width / 2, rowY + 25, 0xFFFFFF);
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Berechne Bereiche für Klickerkennung
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        int sectionTop = PADDING * 3 + fontRendererObj.FONT_HEIGHT;
        int sectionHeight = (height - sectionTop - PADDING * 2 - BUTTON_HEIGHT) / 2;
        
        // Prüfe Klicks auf aktive Events
        if (isMouseInArea(mouseX, mouseY, PADDING, sectionTop + 15, width / 2 - PADDING * 2, sectionHeight - 15)) {
            int rowHeight = 45;
            int clickedIndex = (mouseY - (sectionTop + 15)) / rowHeight;
            
            if (clickedIndex >= 0 && clickedIndex < eventHandler.getActiveEvents().size()) {
                selectedEventIndex = clickedIndex;
                selectedInvestmentIndex = -1;
                investButton.enabled = true;
            }
        }
        // Prüfe Klicks auf aktive Investitionen
        else if (isMouseInArea(mouseX, mouseY, width / 2 + PADDING, sectionTop + 15, width / 2 - PADDING * 2, 
                height - sectionTop - PADDING * 2 - BUTTON_HEIGHT - 15)) {
            int rowHeight = 35;
            int clickedIndex = (mouseY - (sectionTop + 15)) / rowHeight;
            
            if (clickedIndex >= 0 && clickedIndex < eventHandler.getActiveInvestments().size()) {
                selectedInvestmentIndex = clickedIndex;
                selectedEventIndex = -1;
                investButton.enabled = false;
            }
        }
        // Klick außerhalb der Listen deselektiert alles
        else if (mouseButton == 0) {
            selectedEventIndex = -1;
            selectedInvestmentIndex = -1;
            investButton.enabled = false;
        }
    }
    
    private boolean isMouseInArea(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Back
                Minecraft.getMinecraft().displayGuiScreen(new FlippidyGui());
                break;
                
            case 2: // Refresh
                // Hier könnten wir Events aktualisieren
                break;
                
            case 3: // Invest
                if (selectedEventIndex >= 0 && selectedEventIndex < eventHandler.getActiveEvents().size()) {
                    EventHandler.SkyblockEvent selectedEvent = eventHandler.getActiveEvents().get(selectedEventIndex);
                    // Öffne Invest-Dialog
                    Minecraft.getMinecraft().displayGuiScreen(new EventInvestGui(selectedEvent));
                }
                break;
        }
    }
}