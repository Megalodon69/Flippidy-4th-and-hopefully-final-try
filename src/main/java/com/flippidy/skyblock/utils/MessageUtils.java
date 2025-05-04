package com.flippidy.skyblock.utils;

import com.flippidy.skyblock.FlippidyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class MessageUtils {
    private static final String PREFIX = "§a[Flippidy] §r";
    private static final String DEBUG_PREFIX = "§b[Flippidy Debug] §r";
    
    /**
     * Sendet eine Nachricht im Minecraft-Chat
     * @param message Die zu sendende Nachricht
     */
    public static void sendMessage(String message) {
        if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(PREFIX + message));
        }
    }
    
    /**
     * Sendet eine Debug-Nachricht im Minecraft-Chat, wenn der Debug-Modus aktiviert ist
     * @param message Die zu sendende Debug-Nachricht
     */
    public static void sendDebugMessage(String message) {
        if (FlippidyMod.getInstance().isDebugMode() && 
            Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(DEBUG_PREFIX + message));
        }
    }
}