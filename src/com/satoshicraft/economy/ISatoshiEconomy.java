package com.satoshicraft.economy;

import com.satoshicraft.economy.economy.EconomyManager;
import com.satoshicraft.economy.economy.logger.TransactionLogger;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public interface ISatoshiEconomy {
    /**
     * Get the active EconomyManager.
     * @return EconomyManager
     */
    EconomyManager getEconomyManager();

    /**
     * Check whether transactions should be logged.
     * @return True if transactions should be logged, false otherwise.
     */
    boolean shouldLogTransactions();

    /**
     * Get the active TransactionLogger.
     * @return TransactionLogger, if there is one.
     * @throws IllegalStateException if shouldLogTransactions() is false.
     */
    TransactionLogger getTransactionLogger();
}
