package com.satoshicraft.economy.economy.logger;

import com.satoshicraft.economy.economy.transaction.Transaction;

/**
 * Created by AppleDash on 8/15/2016.
 * Blackjack is still best pony.
 */
public interface TransactionLogger {
    void logTransaction(Transaction transaction);
}
