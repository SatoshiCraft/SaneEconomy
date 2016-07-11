package org.appledash.saneeconomy.economy.backend;

import org.bukkit.OfflinePlayer;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents our economy storage backend - whatever we're using to store economy data.
 */
public interface EconomyStorageBackend {
    /**
     * Check whether a player has used the economy system before.
     * @param player Player
     * @return True if they have, false otherwise.
     */
    boolean accountExists(OfflinePlayer player);

    /**
     * Get the balance of a player.
     * @param player Player
     * @return Player's current balance
     */
    double getBalance(OfflinePlayer player);

    /**
     * Set the balance of a player, overwriting the old balance.
     * @param player Player
     * @param newBalance Player's new balance
     */
    void setBalance(OfflinePlayer player, double newBalance);

    /**
     * Add to a player's balance.
     * @param player Player
     * @param amount Amount to add to the balance
     * @return Player's new balance
     */
    double addBalance(OfflinePlayer player, double amount);

    /**
     * Subtract from a player's balance.
     * @param player Player
     * @param amount Amount to subtract from the balance
     * @return Player's new balance
     */
    double subtractBalance(OfflinePlayer player, double amount);

    /**
     * Reload this backend's database from disk.
     */
    void reloadDatabase();
}