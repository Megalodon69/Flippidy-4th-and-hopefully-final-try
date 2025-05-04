package com.flippidy.skyblock.gui;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.models.FlipStatistics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class StatisticsGui extends GuiScreen {
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    
    private FlipStatistics statistics;
    private GuiButton backButton;
    
    public StatisticsGui() {
        this.statistics = FlippidyMod.getInstance().getStatistics();
    }
    
    @Override
    public void initGui() {
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        // Erstelle Zurück-Button unten
        backButton = new GuiButton(1, (width - 100) / 2, height - BUTTON_HEIGHT - PADDING, 100, BUTTON_HEIGHT, "Zurück");
        buttonList.add(backButton);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Zeichne Hintergrund
        drawDefaultBackground();
        
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        // Zeichne Überschrift
        String title = EnumChatFormatting.GOLD + "Flippidy - Statistiken";
        fontRendererObj.drawStringWithShadow(title, (width - fontRendererObj.getStringWidth(title)) / 2, PADDING, 0xFFFFFF);
        
        // Startposition für Statistiken
        int y = PADDING * 3;
        int leftCol = width / 4;
        int rightCol = width * 3 / 4;
        
        // Übersichtsstatistiken
        drawSectionHeader("Übersicht", leftCol - 100, y);
        y += 20;
        
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Gesamtgewinn:", leftCol - 100, y, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.GREEN + df.format(statistics.getTotalProfit()) + " Münzen", leftCol + 50, y, 0xFFFFFF);
        y += 15;
        
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Anzahl Flips:", leftCol - 100, y, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.AQUA + String.valueOf(statistics.getTotalFlips()), leftCol + 50, y, 0xFFFFFF);
        y += 15;
        
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Durchschnittlicher Gewinn/Flip:", leftCol - 100, y, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + df.format(statistics.getAverageProfit()) + " Münzen", leftCol + 50, y, 0xFFFFFF);
        y += 15;
        
        if (statistics.getFirstFlipDate() != null) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Erster Flip:", leftCol - 100, y, 0xFFFFFF);
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + DATE_FORMAT.format(statistics.getFirstFlipDate()), leftCol + 50, y, 0xFFFFFF);
            y += 15;
        }
        
        if (statistics.getLastFlipDate() != null) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Letzter Flip:", leftCol - 100, y, 0xFFFFFF);
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + DATE_FORMAT.format(statistics.getLastFlipDate()), leftCol + 50, y, 0xFFFFFF);
            y += 15;
        }
        
        // Meistgeflippte Items
        y += 20;
        drawSectionHeader("Meistgeflippte Items", leftCol - 100, y);
        y += 20;
        
        List<Map.Entry<String, Integer>> topItems = statistics.getMostFlippedItems(5);
        for (int i = 0; i < topItems.size(); i++) {
            Map.Entry<String, Integer> entry = topItems.get(i);
            String itemName = formatItemName(entry.getKey());
            fontRendererObj.drawStringWithShadow(
                    EnumChatFormatting.AQUA + (i+1) + ". " + itemName + ": " + 
                    EnumChatFormatting.YELLOW + entry.getValue() + "x",
                    leftCol - 100, y, 0xFFFFFF);
            y += 15;
        }
        
        // Profitabelste Tage
        y = PADDING * 3 + 20;
        drawSectionHeader("Profitabelste Tage", rightCol - 100, y);
        y += 20;
        
        List<Map.Entry<String, Double>> topDays = statistics.getMostProfitableDays(5);
        for (int i = 0; i < topDays.size(); i++) {
            Map.Entry<String, Double> entry = topDays.get(i);
            fontRendererObj.drawStringWithShadow(
                    EnumChatFormatting.AQUA + (i+1) + ". " + entry.getKey() + ": " + 
                    EnumChatFormatting.GOLD + df.format(entry.getValue()) + " Münzen",
                    rightCol - 100, y, 0xFFFFFF);
            y += 15;
        }
        
        // Zeichne Tagesprofithistogramm
        y += 20;
        drawSectionHeader("Tagesverlauf", rightCol - 100, y);
        y += 20;
        
        Map<String, Double> dailyProfits = statistics.getDailyProfits();
        if (!dailyProfits.isEmpty()) {
            int chartWidth = width / 3;
            int chartHeight = 100;
            int chartX = rightCol - 100;
            int chartY = y;
            
            // Finde maximalen Tagesprofit für die Skalierung
            double maxProfit = dailyProfits.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            
            // Zeichne Chart-Hintergrund
            drawRect(chartX, chartY, chartX + chartWidth, chartY + chartHeight, new Color(30, 30, 30, 180).getRGB());
            
            // Zeichne Balken für jeden Tag
            int barWidth = Math.max(5, chartWidth / dailyProfits.size());
            int x = chartX;
            
            for (Map.Entry<String, Double> entry : dailyProfits.entrySet()) {
                double profit = entry.getValue();
                int barHeight = (int) (profit / maxProfit * chartHeight);
                
                drawRect(x, chartY + chartHeight - barHeight, x + barWidth - 1, chartY + chartHeight, 
                        new Color(50, 200, 50, 200).getRGB());
                
                x += barWidth;
                if (x > chartX + chartWidth) break;
            }
            
            // Zeichne Y-Achse Beschriftung
            fontRendererObj.drawStringWithShadow(
                    EnumChatFormatting.GRAY + "Max: " + EnumChatFormatting.GREEN + df.format(maxProfit),
                    chartX, chartY - 10, 0xFFFFFF);
        } else {
            fontRendererObj.drawStringWithShadow(
                    EnumChatFormatting.GRAY + "Keine Tagesdaten verfügbar",
                    rightCol - 100, y, 0xFFFFFF);
        }
        
        // Zeichne Buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawSectionHeader(String title, int x, int y) {
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + "§l" + title, x, y, 0xFFFFFF);
        drawHorizontalLine(x, x + 200, y + fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) { // Zurück-Button
            Minecraft.getMinecraft().displayGuiScreen(new FlippidyGui());
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