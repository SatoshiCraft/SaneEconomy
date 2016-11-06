package com.satoshicraft.economy.vault;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import com.satoshicraft.economy.SatoshiEconomy;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class VaultHook {
    private final SatoshiEconomy plugin;
    private final Economy provider = new EconomySaneEconomy();

    public VaultHook(SatoshiEconomy plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Normal);
    }

    public void unhook() {
        Bukkit.getServicesManager().unregister(Economy.class, provider);
    }
}
