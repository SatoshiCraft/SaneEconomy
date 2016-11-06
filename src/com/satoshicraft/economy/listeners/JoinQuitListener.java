package com.satoshicraft.economy.listeners;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.economy.economable.Economable;
import com.satoshicraft.economy.economy.transaction.Transaction;
import com.satoshicraft.economy.economy.transaction.TransactionReason;
import com.satoshicraft.economy.utils.MessageUtils;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class JoinQuitListener implements Listener {
    private final SatoshiEconomy plugin;

    public JoinQuitListener(SatoshiEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        Economable economable = Economable.wrap((OfflinePlayer) player);
        double startBalance = plugin.getConfig().getDouble("economy.start-balance", 0.0D);

        /* A starting balance is configured AND they haven't been given it yet. */
        if ((startBalance > 0) && !plugin.getEconomyManager().accountExists(economable)) {
            plugin.getEconomyManager().transact(new Transaction(
                    Economable.CONSOLE, economable, startBalance, TransactionReason.STARTING_BALANCE
            ));
            MessageUtils.sendMessage(player, "You've been issued a starting balance of %s!", plugin.getEconomyManager().getCurrency().formatAmount(startBalance));
        }

    }
}
