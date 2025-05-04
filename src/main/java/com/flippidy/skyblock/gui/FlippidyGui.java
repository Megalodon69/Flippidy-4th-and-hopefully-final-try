package com.flippidy.skyblock.gui;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.models.FlipCandidate;
import com.flippidy.skyblock.models.UserProfile;
import com.flippidy.skyblock.utils.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class FlippidyGui extends GuiScreen {
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    private final FlippidyMod mod;
    private List<FlipCandidate> flipCandidates;
    private int scrollOffset = 0;
    private boolean isScrolling = false;
    private GuiButton refreshButton;
    private GuiButton statsButton;
    private GuiButton profileButton;
    private GuiButton filterButton;
    private GuiButton eventsButton;
    private GuiButton startStopButton;
    
    public FlippidyGui() {
        this.mod = FlippidyMod.getInstance();
        this.flipCandidates = mod.getBazaarDataHandler().getFlipCandidates();
    }
    
    @Override
    public void initGui() {
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        int buttonWidth = 80;
        int buttonSpacing = 5;
        int startX = (width - (buttonWidth * 6 + buttonSpacing * 5)) / 2;
        int buttonY = height - BUTTON_HEIGHT - PADDING;
        
        // Erstelle Buttons am unteren Rand
        refreshButton = new GuiButton(1, startX, buttonY, buttonWidth, BUTTON_HEIGHT, "Aktualisieren");
        buttonList.add(refreshButton);
        
        startStopButton = new GuiButton(2, startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, BUTTON_HEIGHT, 
                mod.isRunning() ? "Stoppen" : "Starten");
        buttonList.add(startStopButton);
        
        statsButton = new GuiButton(3, startX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, BUTTON_HEIGHT, "Statistiken");
        buttonList.add(statsButton);
        
        profileButton = new GuiButton(4, startX + (buttonWidth + buttonSpacing) * 3, buttonY, buttonWidth, BUTTON_HEIGHT, "Profile");
        buttonList.add(profileButton);
        
        filterButton = new GuiButton(5, startX + (buttonWidth + buttonSpacing) * 4, buttonY, buttonWidth, BUTTON_HEIGHT, "Filter");
        buttonList.add(filterButton);
        
        eventsButton = new GuiButton(6, startX + (buttonWidth + buttonSpacing) * 5, buttonY, buttonWidth, BUTTON_HEIGHT, "Events");
        buttonList.add(eventsButton);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Zeichne Hintergrund
        drawDefaultBackground();
        
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        // Zeichne Überschrift
        String title = EnumChatFormatting.GOLD + "Flippidy - Bazaar Flip Helper";
        fontRendererObj.drawStringWithShadow(title, (width - fontRendererObj.getStringWidth(title)) / 2, PADDING, 0xFFFFFF);
        
        // Zeichne Statusinfo
        String statusText = EnumChatFormatting.GRAY + "Status: " + 
                (mod.isRunning() ? EnumChatFormatting.GREEN + "Aktiv" : EnumChatFormatting.RED + "Inaktiv");
        fontRendererObj.drawStringWithShadow(statusText, PADDING, PADDING, 0xFFFFFF);
        
        // Zeichne Gesamtgewinn
        String profitText = EnumChatFormatting.GRAY + "Gesamtgewinn: " + 
                EnumChatFormatting.GOLD + df.format(mod.getTotalProfit()) + " Münzen";
        fontRendererObj.drawStringWithShadow(profitText, width - PADDING - fontRendererObj.getStringWidth(profitText), PADDING, 0xFFFFFF);
        
        // Zeichne Profilinformationen
        UserProfile activeProfile = mod.getProfileManager().getActiveProfile();
        if (activeProfile != null) {
            String profileText = EnumChatFormatting.GRAY + "Profil: " + 
                    EnumChatFormatting.AQUA + activeProfile.getName();
            fontRendererObj.drawStringWithShadow(profileText, PADDING, PADDING * 2 + fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
        }
        
        // Zeichne Tabelle
        drawFlipTable(mouseX, mouseY);
        
        // Zeichne Buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawFlipTable(int mouseX, int mouseY) {
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int tableTop = PADDING * 4 + fontRendererObj.FONT_HEIGHT * 2;
        int tableBottom = height - PADDING * 2 - BUTTON_HEIGHT;
        int tableHeight = tableBottom - tableTop;
        int rowHeight = fontRendererObj.FONT_HEIGHT + 6;
        
        // Zeichne Tabellenüberschriften
        String[] headers = {"Item", "Kaufpreis", "Verkaufspreis", "Gewinn/Item", "Gesamtgewinn", "Menge"};
        int[] columnWidths = {width / 3, width / 9, width / 9, width / 9, width / 9, width / 9};
        int startX = PADDING;
        
        // Hintergrund für Überschriften
        drawRect(startX, tableTop, width - PADDING, tableTop + rowHeight, new Color(0, 0, 0, 200).getRGB());
        
        // Überschriften
        int headerX = startX + 5;
        for (int i = 0; i < headers.length; i++) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + headers[i], headerX, tableTop + 3, 0xFFFFFF);
            headerX += columnWidths[i];
        }
        
        // Zeichne Zeilen
        if (flipCandidates == null || flipCandidates.isEmpty()) {
            String noDataText = EnumChatFormatting.GRAY + "Keine Flips gefunden. Klicke auf 'Aktualisieren'.";
            fontRendererObj.drawStringWithShadow(
                    noDataText, 
                    (width - fontRendererObj.getStringWidth(noDataText)) / 2, 
                    tableTop + rowHeight + 20, 
                    0xFFFFFF
            );
            return;
        }
        
        // Berechne die maximale Anzahl von Zeilen, die angezeigt werden können
        int maxVisibleRows = (tableHeight - rowHeight) / rowHeight;
        int maxOffset = Math.max(0, flipCandidates.size() - maxVisibleRows);
        scrollOffset = Math.min(scrollOffset, maxOffset); // Begrenze den Scroll-Offset
        
        // Zeichne sichtbare Zeilen
        for (int i = scrollOffset; i < Math.min(scrollOffset + maxVisibleRows, flipCandidates.size()); i++) {
            FlipCandidate flip = flipCandidates.get(i);
            int rowY = tableTop + rowHeight + (i - scrollOffset) * rowHeight;
            
            // Zeile abwechselnd einfärben
            drawRect(startX, rowY, width - PADDING, rowY + rowHeight, 
                    (i % 2 == 0) ? new Color(30, 30, 30, 180).getRGB() : new Color(50, 50, 50, 180).getRGB());
            
            // Zeilendaten zeichnen
            int colX = startX + 5;
            
            // Item Name
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.AQUA + formatItemName(flip.getItem().getProductId()), 
                    colX, rowY + 3, 0xFFFFFF);
            colX += columnWidths[0];
            
            // Kaufpreis
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GREEN + df.format(flip.getItem().getBestSellPrice()), 
                    colX, rowY + 3, 0xFFFFFF);
            colX += columnWidths[1];
            
            // Verkaufspreis
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GREEN + df.format(flip.getItem().getBestBuyPrice()), 
                    colX, rowY + 3, 0xFFFFFF);
            colX += columnWidths[2];
            
            // Gewinn pro Item
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + df.format(flip.getProfitPerItem()), 
                    colX, rowY + 3, 0xFFFFFF);
            colX += columnWidths[3];
            
            // Gesamtgewinn
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + df.format(flip.getTotalProfit()), 
                    colX, rowY + 3, 0xFFFFFF);
            colX += columnWidths[4];
            
            // Menge
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "" + flip.getMaxItems(), 
                    colX, rowY + 3, 0xFFFFFF);
        }
        
        // Scrollbalken zeichnen, wenn nötig
        if (flipCandidates.size() > maxVisibleRows) {
            int scrollBarHeight = tableHeight - rowHeight;
            int thumbHeight = Math.max(30, scrollBarHeight * maxVisibleRows / flipCandidates.size());
            int thumbY = tableTop + rowHeight + (scrollOffset * (scrollBarHeight - thumbHeight) / maxOffset);
            
            // Scrollbar-Hintergrund
            drawRect(width - PADDING - 8, tableTop + rowHeight, width - PADDING, tableBottom, new Color(30, 30, 30, 100).getRGB());
            
            // Scrollbar-Thumb
            drawRect(width - PADDING - 8, thumbY, width - PADDING, thumbY + thumbHeight, new Color(150, 150, 150, 200).getRGB());
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Refresh
                refreshData();
                break;
                
            case 2: // Start/Stop
                toggleFlipping();
                break;
                
            case 3: // Statistics
                Minecraft.getMinecraft().displayGuiScreen(new StatisticsGui());
                break;
                
            case 4: // Profiles
                Minecraft.getMinecraft().displayGuiScreen(new ProfileGui());
                break;
                
            case 5: // Filters
                Minecraft.getMinecraft().displayGuiScreen(new FilterGui());
                break;
                
            case 6: // Events
                Minecraft.getMinecraft().displayGuiScreen(new EventsGui());
                break;
        }
    }
    
    private void refreshData() {
        MessageUtils.sendMessage("§aAktualisiere Bazaar-Daten...");
        
        mod.getBazaarDataHandler().refreshBazaarData().thenRun(() -> {
            double purseCoins = mod.getProfileManager().getActiveProfile() != null ? 
                    mod.getProfileManager().getActiveProfile().getMaxInvestmentPercentage() : 30.0;
            mod.getBazaarDataHandler().calculateFlips(purseCoins).thenRun(() -> {
                flipCandidates = mod.getBazaarDataHandler().getFlipCandidates();
                MessageUtils.sendMessage("§aDaten aktualisiert! " + flipCandidates.size() + " potenzielle Flips gefunden.");
            });
        });
    }
    
    private void toggleFlipping() {
        if (mod.isRunning()) {
            mod.setRunning(false);
            startStopButton.displayString = "Starten";
            MessageUtils.sendMessage("§cFlip-Algorithmus wurde gestoppt.");
        } else {
            mod.setRunning(true);
            startStopButton.displayString = "Stoppen";
            MessageUtils.sendMessage("§aFlip-Algorithmus wurde gestartet.");
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int mouseScroll = Mouse.getEventDWheel();
        if (mouseScroll != 0) {
            scrollOffset += (mouseScroll > 0) ? -1 : 1;
            if (scrollOffset < 0) scrollOffset = 0;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Implementiere hier Klicklogik für Tabelle, z.B. um einen Flip auszuwählen
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