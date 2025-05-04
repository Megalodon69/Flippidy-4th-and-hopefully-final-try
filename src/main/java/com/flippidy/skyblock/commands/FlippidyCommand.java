package com.flippidy.skyblock.commands;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.models.FlipCandidate;
import com.flippidy.skyblock.utils.ApiUtils;
import com.flippidy.skyblock.utils.MessageUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FlippidyCommand extends CommandBase {
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    @Override
    public String getCommandName() {
        return "flippidy";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/flippidy <start|stop|help|debug|stats|profile|gui|filter|events>";
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("flip", "flippy");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "help", "debug", "stats", "profile", "gui", "filter", "events");
        } else if (args.length == 2) {
            String command = args[0].toLowerCase();
            if (command.equals("profile")) {
                return Arrays.asList("create", "load", "list");
            } else if (command.equals("filter")) {
                return Arrays.asList("set", "clear", "show");
            } else if (command.equals("events")) {
                return Arrays.asList("list", "invest", "settings");
            }
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        FlippidyMod mod = FlippidyMod.getInstance();
        
        if (args.length == 0) {
            showHelp();
            return;
        }
        
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "start":
                startFlipping();
                break;
                
            case "stop":
                stopFlipping();
                break;
                
            case "help":
                showHelp();
                break;
                
            case "debug":
                toggleDebug();
                break;
                
            case "stats":
                showStatistics();
                break;
                
            case "gui":
                openGui();
                break;
                
            case "profile":
                handleProfileCommand(args);
                break;
                
            case "filter":
                handleFilterCommand(args);
                break;
                
            case "events":
                handleEventsCommand(args);
                break;
                
            default:
                MessageUtils.sendMessage("§cUnbekannter Unterbefehl. Verwende /flippidy help für Hilfe.");
                break;
        }
    }
    
    private void startFlipping() {
        FlippidyMod mod = FlippidyMod.getInstance();
        
        if (mod.isRunning()) {
            MessageUtils.sendMessage("§cFlippidy ist bereits aktiv!");
            return;
        }
        
        MessageUtils.sendMessage("§aStarte Flippidy...");
        mod.setRunning(true);
        mod.resetProfit();
        
        // Starte den Flip-Prozess
        CompletableFuture.runAsync(() -> {
            while (mod.isRunning()) {
                try {
                    // Aktualisiere Basar-Daten
                    mod.getBazaarDataHandler().refreshBazaarData().thenRun(() -> {
                        // Berechne Flips basierend auf dem Spieler-Purse
                        double purseCoins = ApiUtils.fetchPlayerPurse();
                        mod.getBazaarDataHandler().calculateFlips(purseCoins).thenRun(() -> {
                            // Zeige die besten Flips an
                            displayBestFlips();
                        });
                    });
                    
                    // Warte 1 Minute, bevor wir die Daten wieder aktualisieren
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    MessageUtils.sendMessage("§cFehler beim Ausführen des Flip-Algorithmus: " + e.getMessage());
                    e.printStackTrace();
                    mod.setRunning(false);
                    break;
                }
            }
        });
    }
    
    private void stopFlipping() {
        FlippidyMod mod = FlippidyMod.getInstance();
        
        if (!mod.isRunning()) {
            MessageUtils.sendMessage("§cFlippidy ist nicht aktiv!");
            return;
        }
        
        mod.setRunning(false);
        MessageUtils.sendMessage("§aFlippidy wurde gestoppt. Gesamtgewinn: §6" + df.format(mod.getTotalProfit()) + " Münzen");
    }
    
    private void showHelp() {
        MessageUtils.sendMessage("§6§l--- Flippidy Hilfe ---");
        MessageUtils.sendMessage("§6/flippidy start §f- Startet den automatischen Flip-Algorithmus");
        MessageUtils.sendMessage("§6/flippidy stop §f- Stoppt den Flip-Algorithmus");
        MessageUtils.sendMessage("§6/flippidy debug §f- Aktiviert/Deaktiviert Debug-Nachrichten");
        MessageUtils.sendMessage("§6/flippidy stats §f- Zeigt Statistiken an");
        MessageUtils.sendMessage("§6/flippidy gui §f- Öffnet die GUI");
        MessageUtils.sendMessage("§6/flippidy profile §f- Verwalte Profile (create, load, list)");
        MessageUtils.sendMessage("§6/flippidy filter §f- Verwalte Filter (set, clear, show)");
        MessageUtils.sendMessage("§6/flippidy events §f- Verwalte Events (list, invest, settings)");
        MessageUtils.sendMessage("§6/flippidy help §f- Zeigt diese Hilfe an");
        MessageUtils.sendMessage("§7§oDieser Mod dient als Proof of Concept für die Hypixel-Admins.");
    }
    
    private void toggleDebug() {
        FlippidyMod mod = FlippidyMod.getInstance();
        mod.setDebugMode(!mod.isDebugMode());
        
        if (mod.isDebugMode()) {
            MessageUtils.sendMessage("§aDebug-Modus §2aktiviert§a.");
        } else {
            MessageUtils.sendMessage("§aDebug-Modus §cdeaktiviert§a.");
        }
    }
    
    private void displayBestFlips() {
        FlippidyMod mod = FlippidyMod.getInstance();
        List<FlipCandidate> flipCandidates = mod.getBazaarDataHandler().getFlipCandidates();
        
        if (flipCandidates.isEmpty()) {
            MessageUtils.sendMessage("§eKeine profitablen Flips gefunden.");
            return;
        }
        
        MessageUtils.sendMessage("§a§l--- Beste Flips ---");
        
        for (int i = 0; i < Math.min(5, flipCandidates.size()); i++) {
            FlipCandidate flip = flipCandidates.get(i);
            double buyPrice = flip.getItem().getBestSellPrice();
            double sellPrice = flip.getItem().getBestBuyPrice();
            
            MessageUtils.sendMessage(
                    "§6" + (i+1) + ". §b" + formatItemName(flip.getItem().getProductId()) + 
                    " §7- Kaufen: §a" + df.format(buyPrice) + 
                    " §7Verkaufen: §a" + df.format(sellPrice) + 
                    " §7Gewinn/Item: §a" + df.format(flip.getProfitPerItem()) + 
                    " §7Gesamtgewinn: §a" + df.format(flip.getTotalProfit()) + 
                    " §7(x" + flip.getMaxItems() + ")"
            );
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
    
    private void showStatistics() {
        // Implementiere die Logik, um Statistiken anzuzeigen
        MessageUtils.sendMessage("§aStatistiken werden hier angezeigt.");
    }
    
    private void openGui() {
        // Implementiere die Logik, um die GUI zu öffnen
        MessageUtils.sendMessage("§aGUI wird hier geöffnet.");
    }
    
    private void handleProfileCommand(String[] args) {
        // Implementiere die Logik, um Profile zu verwalten
        if (args.length < 2) {
            MessageUtils.sendMessage("§cVerwende /flippidy profile <create|load|list>");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "create":
                MessageUtils.sendMessage("§aProfil wird erstellt.");
                break;
            case "load":
                MessageUtils.sendMessage("§aProfil wird geladen.");
                break;
            case "list":
                MessageUtils.sendMessage("§aProfile werden aufgelistet.");
                break;
            default:
                MessageUtils.sendMessage("§cUnbekannter Unterbefehl für profile.");
                break;
        }
    }
    
    private void handleFilterCommand(String[] args) {
        // Implementiere die Logik, um Filter zu verwalten
        if (args.length < 2) {
            MessageUtils.sendMessage("§cVerwende /flippidy filter <set|clear|show>");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "set":
                MessageUtils.sendMessage("§aFilter wird gesetzt.");
                break;
            case "clear":
                MessageUtils.sendMessage("§aFilter wird gelöscht.");
                break;
            case "show":
                MessageUtils.sendMessage("§aFilter wird angezeigt.");
                break;
            default:
                MessageUtils.sendMessage("§cUnbekannter Unterbefehl für filter.");
                break;
        }
    }
    
    private void handleEventsCommand(String[] args) {
        // Implementiere die Logik, um Events zu verwalten
        if (args.length < 2) {
            MessageUtils.sendMessage("§cVerwende /flippidy events <list|invest|settings>");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "list":
                MessageUtils.sendMessage("§aEvents werden aufgelistet.");
                break;
            case "invest":
                MessageUtils.sendMessage("§aInvestition in Events wird durchgeführt.");
                break;
            case "settings":
                MessageUtils.sendMessage("§aEvent-Einstellungen werden angezeigt.");
                break;
            default:
                MessageUtils.sendMessage("§cUnbekannter Unterbefehl für events.");
                break;
        }
    }
}