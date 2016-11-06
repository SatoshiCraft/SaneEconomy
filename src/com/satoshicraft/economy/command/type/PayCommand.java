package com.satoshicraft.economy.command.type;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.command.SaneEconomyCommand;
import com.satoshicraft.economy.command.exception.CommandException;
import com.satoshicraft.economy.command.exception.type.usage.NeedPlayerException;
import com.satoshicraft.economy.economy.EconomyManager;
import com.satoshicraft.economy.economy.economable.Economable;
import com.satoshicraft.economy.economy.transaction.Transaction;
import com.satoshicraft.economy.economy.transaction.TransactionReason;
import com.satoshicraft.economy.economy.transaction.TransactionResult;
import com.satoshicraft.economy.utils.MessageUtils;
import com.satoshicraft.economy.utils.NumberUtils;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 *
 * TODO: Support for paying offline players.
 */
public class PayCommand extends SaneEconomyCommand {
    public PayCommand(SatoshiEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.pay";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/pay <player> <amount>"
        };
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw CommandException.makeArgumentException(2, args.length);
        }

        /* Doesn't make sense for console to pay a player, and admins paying a player is best done with /ecoadmin give */
        if (!(sender instanceof Player)) {
            throw new NeedPlayerException();
        }

        EconomyManager ecoMan = saneEconomy.getEconomyManager();
        Player fromPlayer = (Player) sender;

        String sToPlayer = args[0];
        Player toPlayer = Bukkit.getServer().getPlayer(sToPlayer);

        if (toPlayer == null) {
            MessageUtils.sendMessage(sender, "That player is not online.");
            return;
        }

        if (toPlayer.getPlayer().equals(fromPlayer.getPlayer())) {
            MessageUtils.sendMessage(sender, "You cannot pay yourself.");
            return;
        }

        String sAmount = args[1];
        double amount = NumberUtils.parseAndFilter(ecoMan.getCurrency(), sAmount);

        if (amount <= 0) {
            MessageUtils.sendMessage(sender, "%s is not a positive number.", ((amount == -1) ? sAmount : String.valueOf(amount)));
            return;
        }

        /* Perform the actual transfer. False == They didn't have enough money */
        Transaction transaction = new Transaction(Economable.wrap(fromPlayer), Economable.wrap(toPlayer), amount, TransactionReason.PLAYER_PAY);
        TransactionResult result = ecoMan.transact(transaction);

        if (result.getStatus() != TransactionResult.Status.SUCCESS) {
            MessageUtils.sendMessage(sender, "You do not have enough money to transfer %s to %s.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sToPlayer
            );

            return;
        }

        /* Inform the relevant parties. */

        MessageUtils.sendMessage(sender, "You have transferred %s to %s.",
                ecoMan.getCurrency().formatAmount(amount),
                sToPlayer
        );

        MessageUtils.sendMessage(toPlayer, "You have received %s from %s.",
                ecoMan.getCurrency().formatAmount(amount),
                fromPlayer.getDisplayName()
        );
    }
}
