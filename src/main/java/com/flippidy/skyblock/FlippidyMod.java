package com.flippidy.skyblock;

import com.flippidy.skyblock.commands.FlippidyCommand;
import com.flippidy.skyblock.handlers.BazaarDataHandler;
import com.flippidy.skyblock.models.FlipStatistics;
import com.flippidy.skyblock.utils.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mod(modid = FlippidyMod.MODID, version = FlippidyMod.VERSION, name = FlippidyMod.NAME)
public class FlippidyMod {
    public static final String MODID = "flippidy";
    public static final String VERSION = "1.0";
    public static final String NAME = "Flippidy";

    private static FlippidyMod instance;
    private BazaarDataHandler bazaarDataHandler;
    private boolean isRunning = false;
    private boolean isDebugMode = false;
    private double totalProfit = 0.0;
    private FlipStatistics statistics;
    private File configDir;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;

        // Initialisiere Konfigurationsverzeichnis
        configDir = new File(event.getModConfigurationDirectory(), MODID);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Lade Statistiken
        statistics = new FlipStatistics(new File(configDir, "statistics.json"));
        statistics.load();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);

        // Initialize handlers
        this.bazaarDataHandler = new BazaarDataHandler();

        // Register commands
        ClientCommandHandler.instance.registerCommand(new FlippidyCommand());
    }

    public static FlippidyMod getInstance() {
        return instance;
    }

    public BazaarDataHandler getBazaarDataHandler() {
        return bazaarDataHandler;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.isDebugMode = debugMode;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void addProfit(double profit) {
        this.totalProfit += profit;

        // Füge zur Gesamtstatistik hinzu
        statistics.addProfit(profit);

        // Speichere Statistik nach jeder Änderung
        statistics.save();
    }

    public void resetProfit() {
        this.totalProfit = 0.0;
    }

    public FlipStatistics getStatistics() {
        return statistics;
    }

    public File getConfigDir() {
        return configDir;
    }
}