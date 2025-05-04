package com.flippidy.skyblock.gui;

import com.flippidy.skyblock.FlippidyMod;
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

public class FilterGui extends GuiScreen {
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    private FlippidyMod mod;
    private UserProfile profile;
    private GuiButton backButton;
    private GuiButton saveButton;
    private GuiButton showUnstableItemsButton;
    private GuiButton showLowVolumeItemsButton;
    private GuiButton prioritizeFavoritesButton;
    private GuiTextField minProfitField;
    private GuiTextField maxInvestmentField;
    private GuiTextField maxEventInvestmentField;
    
    public FilterGui() {
        this.mod = FlippidyMod.getInstance();
        this.profile = mod.getProfileManager().getActiveProfile();
    }
    
    @Override
    public void initGui() {
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        
        int buttonWidth = 200;
        int buttonX = (width - buttonWidth) / 2;
        int startY = PADDING * 5 + fontRendererObj.FONT_HEIGHT * 2;
        
        // Toggle-Buttons für Filtereinstellungen
        showUnstableItemsButton = new GuiButton(1, buttonX, startY, buttonWidth, BUTTON_HEIGHT, 
                "Instabile Items: " + (profile.getFilterSettings().getOrDefault("showUnstableItems", true) ? "§aAn" : "§cAus"));
        buttonList.add(showUnstableItemsButton);
        
        showLowVolumeItemsButton = new GuiButton(2, buttonX, startY + BUTTON_HEIGHT + 5, buttonWidth, BUTTON_HEIGHT, 
                "Items mit niedrigem Volumen: " + (profile.getFilterSettings().getOrDefault("showLowVolumeItems", false) ? "§aAn" : "§cAus"));
        buttonList.add(showLowVolumeItemsButton);
        
        prioritizeFavoritesButton = new GuiButton(3, buttonX, startY + (BUTTON_HEIGHT + 5) * 2, buttonWidth, BUTTON_HEIGHT, 
                "Favoriten priorisieren: " + (profile.getFilterSettings().getOrDefault("prioritizeFavorites", true) ? "§aAn" : "§cAus"));
        buttonList.add(prioritizeFavoritesButton);
        
        // Textfelder für numerische Werte
        minProfitField = new GuiTextField(4, fontRendererObj, buttonX, startY + (BUTTON_HEIGHT + 5) * 3 + 15, buttonWidth, BUTTON_HEIGHT);
        minProfitField.setMaxStringLength(10);
        minProfitField.setText(String.valueOf(profile.getMinProfitMargin()));
        
        maxInvestmentField = new GuiTextField(5, fontRendererObj, buttonX, startY + (BUTTON_HEIGHT + 5) * 4 + 30, buttonWidth, BUTTON_HEIGHT);
        maxInvestmentField.setMaxStringLength(10);
        maxInvestmentField.setText(String.valueOf(profile.getMaxInvestmentPercentage()));
        
        maxEventInvestmentField = new GuiTextField(6, fontRendererObj, buttonX, startY + (BUTTON_HEIGHT + 5) * 5 + 45, buttonWidth, BUTTON_HEIGHT);
        maxEventInvestmentField.setMaxStringLength(10);
        maxEventInvestmentField.setText(String.valueOf(profile.getMaxEventInvestmentPercentage()));
        
        // Action-Buttons
        saveButton = new GuiButton(7, buttonX, startY + (BUTTON_HEIGHT + 5) * 6 + 60, 95, BUTTON_HEIGHT, "Speichern");
        buttonList.add(saveButton);
        
        backButton = new GuiButton(8, buttonX + buttonWidth - 95, startY + (BUTTON_HEIGHT + 5) * 6 + 60, 95, BUTTON_HEIGHT, "Zurück");
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
        String title = EnumChatFormatting.GOLD + "Flippidy - Filtereinstellungen";
        fontRendererObj.drawStringWithShadow(title, (width - fontRendererObj.getStringWidth(title)) / 2, PADDING, 0xFFFFFF);
        
        // Zeichne Profilinformation
        String profileText = EnumChatFormatting.GRAY + "Aktives Profil: " + 
                EnumChatFormatting.AQUA + profile.getName();
        fontRendererObj.drawStringWithShadow(profileText, (width - fontRendererObj.getStringWidth(profileText)) / 2, PADDING * 3, 0xFFFFFF);
        
        // Zeichne Beschriftungen für Textfelder
        int buttonWidth = 200;
        int buttonX = (width - buttonWidth) / 2;
        int startY = PADDING * 5 + fontRendererObj.FONT_HEIGHT * 2;
        
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + "Mindestgewinnmarge (%):", 
                buttonX, startY + (BUTTON_HEIGHT + 5) * 3, 0xFFFFFF);
        
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + "Maximale Investition (% des Vermögens):", 
                buttonX, startY + (BUTTON_HEIGHT + 5) * 4 + 15, 0xFFFFFF);
        
        fontRendererObj.drawStringWithShadow(EnumChatFormatting.YELLOW + "Maximale Event-Investition (% des Vermögens):", 
                buttonX, startY + (BUTTON_HEIGHT + 5) * 5 + 30, 0xFFFFFF);
        
        // Zeichne Textfelder
        minProfitField.drawTextBox();
        maxInvestmentField.drawTextBox();
        maxEventInvestmentField.drawTextBox();
        
        // Zeichne Buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        
        // Erlaube nur Zahlen und Punkt in den Textfeldern
        if ((Character.isDigit(typedChar) || typedChar == '.' || typedChar == '\b') || keyCode == 203 || keyCode == 205) {
            if (minProfitField.isFocused()) {
                minProfitField.textboxKeyTyped(typedChar, keyCode);
            } else if (maxInvestmentField.isFocused()) {
                maxInvestmentField.textboxKeyTyped(typedChar, keyCode);
            } else if (maxEventInvestmentField.isFocused()) {
                maxEventInvestmentField.textboxKeyTyped(typedChar, keyCode);
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        minProfitField.mouseClicked(mouseX, mouseY, mouseButton);
        maxInvestmentField.mouseClicked(mouseX, mouseY, mouseButton);
        maxEventInvestmentField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Show Unstable Items
                boolean showUnstable = !profile.getFilterSettings().getOrDefault("showUnstableItems", true);
                profile.setFilterSetting("showUnstableItems", showUnstable);
                showUnstableItemsButton.displayString = "Instabile Items: " + (showUnstable ? "§aAn" : "§cAus");
                break;
                
            case 2: // Show Low Volume Items
                boolean showLowVolume = !profile.getFilterSettings().getOrDefault("showLowVolumeItems", false);
                profile.setFilterSetting("showLowVolumeItems", showLowVolume);
                showLowVolumeItemsButton.displayString = "Items mit niedrigem Volumen: " + (showLowVolume ? "§aAn" : "§cAus");
                break;
                
            case 3: // Prioritize Favorites
                boolean prioritizeFavorites = !profile.getFilterSettings().getOrDefault("prioritizeFavorites", true);
                profile.setFilterSetting("prioritizeFavorites", prioritizeFavorites);
                prioritizeFavoritesButton.displayString = "Favoriten priorisieren: " + (prioritizeFavorites ? "§aAn" : "§cAus");
                break;
                
            case 7: // Save
                saveSettings();
                Minecraft.getMinecraft().displayGuiScreen(new FlippidyGui());
                break;
                
            case 8: // Back
                Minecraft.getMinecraft().displayGuiScreen(new FlippidyGui());
                break;
        }
    }
    
    private void saveSettings() {
        try {
            // Validiere und speichere die numerischen Werte
            double minProfit = Double.parseDouble(minProfitField.getText());
            double maxInvestment = Double.parseDouble(maxInvestmentField.getText());
            double maxEventInvestment = Double.parseDouble(maxEventInvestmentField.getText());
            
            // Begrenze auf sinnvolle Werte
            minProfit = Math.max(0.1, Math.min(minProfit, 100.0));
            maxInvestment = Math.max(1.0, Math.min(maxInvestment, 100.0));
            maxEventInvestment = Math.max(1.0, Math.min(maxEventInvestment, 100.0));
            
            // Aktualisiere Profil
            profile.setMinProfitMargin(minProfit);
            profile.setMaxInvestmentPercentage(maxInvestment);
            profile.setMaxEventInvestmentPercentage(maxEventInvestment);
            
            // Speichere Profil
            mod.getProfileManager().saveProfile(profile);
        } catch (NumberFormatException e) {
            // Ignoriere ungültige Eingaben
        }
    }
}