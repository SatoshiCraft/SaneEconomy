package com.satoshicraft.economy;

import org.bukkit.plugin.java.JavaPlugin;

import com.satoshicraft.economy.command.SaneEconomyCommand;
import com.satoshicraft.economy.command.type.*;
import com.satoshicraft.economy.economy.EconomyManager;
import com.satoshicraft.economy.economy.logger.TransactionLogger;
import com.satoshicraft.economy.listeners.JoinQuitListener;
import com.satoshicraft.economy.updates.GithubVersionChecker;
import com.satoshicraft.economy.utils.I18n;
import com.satoshicraft.economy.utils.SaneEconomyConfiguration;
import com.satoshicraft.economy.vault.VaultHook;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class SatoshiEconomy extends JavaPlugin implements ISatoshiEconomy {
    private static SatoshiEconomy instance;
    private EconomyManager economyManager;
    private VaultHook vaultHook;
    private TransactionLogger transactionLogger;
    private GithubVersionChecker versionChecker;

    private final Map<String, SaneEconomyCommand> COMMANDS = new HashMap<String, SaneEconomyCommand>() {{
        put("balance", new BalanceCommand(SatoshiEconomy.this));
        put("ecoadmin", new EconomyAdminCommand(SatoshiEconomy.this));
        put("pay", new PayCommand(SatoshiEconomy.this));
        put("saneeconomy", new SaneEcoCommand(SatoshiEconomy.this));
        put("balancetop", new BalanceTopCommand(SatoshiEconomy.this));
    }};

    public SatoshiEconomy() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!loadConfig()) { /* Invalid backend type or connection error of some sort */
            shutdown();
            return;
        }

        loadCommands();
        loadListeners();

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            vaultHook = new VaultHook(this);
            vaultHook.hook();
            getLogger().info("Hooked into Vault.");
        } else {
            getLogger().info("Not hooking into Vault because it isn't loaded.");
        }

        versionChecker = new GithubVersionChecker("SaneEconomyCore", this.getDescription().getVersion());
        getServer().getScheduler().scheduleAsyncDelayedTask(this, versionChecker::checkUpdateAvailable);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            economyManager.getBackend().reloadTopPlayerBalances();
        }, 0, (20 * 300) /* Update baltop every 5 minutes */);
        I18n.getInstance().loadTranslations();
    }

    @Override
    public void onDisable() {
        if (vaultHook != null) {
            getLogger().info("Unhooking from Vault.");
            vaultHook.unhook();
        }

        if (economyManager != null) {
            getLogger().info("Flushing database...");
            economyManager.getBackend().waitUntilFlushed();
        }
    }

    private boolean loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (configFile.exists() && getConfig().getBoolean("debug", false)) {
            getLogger().info("Resetting configuration to default since debug == true.");
            configFile.delete();
            saveDefaultConfig();
            reloadConfig();
            getConfig().set("debug", true);
            saveConfig();
        } else {
            saveDefaultConfig();
            reloadConfig();
        }

        SaneEconomyConfiguration config = new SaneEconomyConfiguration(this);

        economyManager = config.loadEconomyBackend();
        transactionLogger = config.loadLogger();

        saveConfig();

        return economyManager != null;
    }

    private void loadCommands() {
        getLogger().info("Initializing commands...");
        COMMANDS.forEach((name, command) -> getCommand(name).setExecutor(command));
        getLogger().info("Initialized commands.");
    }

    private void loadListeners() {
        getLogger().info("Initializing listeners...");
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getLogger().info("Initialized listeners.");
    }

    private void shutdown(){
        getServer().getPluginManager().disablePlugin(this);
    }

    public GithubVersionChecker getVersionChecker() {
        return versionChecker;
    }

    /**
     * Get the active EconomyManager.
     * @return EconomyManager
     */
    @Override
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Check whether transactions should be logged.
     * @return True if transactions should be logged, false otherwise.
     */
    @Override
    public boolean shouldLogTransactions() {
        return transactionLogger != null;
    }

    /**
     * Get the active TransactionLogger.
     * @return TransactionLogger, if there is one.
     */
    @Override
    public TransactionLogger getTransactionLogger() {
        if (!shouldLogTransactions()) {
            throw new IllegalStateException("TransactionLogger should not be retrieved if we aren't logging transactions!");
        }

        return transactionLogger;
    }

    /**
     * Get the current plugin instance.
     * @return Instance
     */
    public static SatoshiEconomy getInstance() {
        return instance;
    }

    /**
     * Get the logger for the plugin.
     * @return Plugin logger.
     */
    public static Logger logger(){
        return instance.getLogger();
    }
}
