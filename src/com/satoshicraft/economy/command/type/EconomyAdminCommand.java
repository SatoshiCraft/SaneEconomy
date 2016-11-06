package com.satoshicraft.economy.command.type;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.command.SaneEconomyCommand;
import com.satoshicraft.economy.command.exception.CommandException;
import com.satoshicraft.economy.command.exception.type.usage.InvalidUsageException;
import com.satoshicraft.economy.command.exception.type.usage.NeedPlayerException;
import com.satoshicraft.economy.command.exception.type.usage.TooFewArgumentsException;
import com.satoshicraft.economy.economy.EconomyManager;
import com.satoshicraft.economy.economy.economable.Economable;
import com.satoshicraft.economy.economy.transaction.Transaction;
import com.satoshicraft.economy.economy.transaction.TransactionReason;
import com.satoshicraft.economy.economy.transaction.TransactionResult;
import com.satoshicraft.economy.utils.MessageUtils;
import com.satoshicraft.economy.utils.NumberUtils;
import com.satoshicraft.economy.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.satoshicraft.economy.utils.I18n._;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyAdminCommand extends SaneEconomyCommand {
    public EconomyAdminCommand(SatoshiEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.ecoadmin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> <give/take/set> [player] <amount>"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new TooFewArgumentsException();
        }

        String subCommand = args[0];
        String sTargetPlayer;
        String sAmount;

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            sTargetPlayer = sender.getName();
            sAmount = args[1];
        } else {
            sTargetPlayer = args[1];
            sAmount = args[2];
        }

        OfflinePlayer targetPlayer = PlayerUtils.getOfflinePlayer(sTargetPlayer);

        if (targetPlayer == null) {
            MessageUtils.sendMessage(sender, _("That player does not exist."));
            return;
        }

        EconomyManager ecoMan = saneEconomy.getEconomyManager();
        Economable economable = Economable.wrap(targetPlayer);

        double amount = NumberUtils.parseAndFilter(ecoMan.getCurrency(), sAmount);

        if (amount <= 0) {
            MessageUtils.sendMessage(sender, _("%s is not a positive number."), ((amount == -1) ? sAmount : String.valueOf(amount)));
            return;
        }

        if (subCommand.equalsIgnoreCase("give")) {
            Transaction transaction = new Transaction(Economable.wrap(sender), Economable.wrap(targetPlayer), amount, TransactionReason.ADMIN_GIVE);
            TransactionResult result = ecoMan.transact(transaction);

            double newAmount = result.getToBalance();

            MessageUtils.sendMessage(sender, _("Added %s to %s. Their balance is now %s."),
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        }

        if (subCommand.equalsIgnoreCase("take")) {
            Transaction transaction = new Transaction(Economable.wrap(targetPlayer), Economable.wrap(sender), amount, TransactionReason.ADMIN_TAKE);
            TransactionResult result = ecoMan.transact(transaction);

            double newAmount = result.getFromBalance();

            MessageUtils.sendMessage(sender, _("Took %s from %s. Their balance is now %s."),
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        }

        if (subCommand.equalsIgnoreCase("set")) {
            double oldBal = ecoMan.getBalance(economable);
            ecoMan.setBalance(economable, amount);
            MessageUtils.sendMessage(sender, _("Balance for %s set to %s."), sTargetPlayer, ecoMan.getCurrency().formatAmount(amount));

            if (saneEconomy.shouldLogTransactions()) {
                // FIXME: This is a silly hack to get it to log.
                if (oldBal > 0.0) {
                    saneEconomy.getTransactionLogger().logTransaction(new Transaction(
                            economable, Economable.CONSOLE, oldBal, TransactionReason.ADMIN_GIVE
                    ));
                }

                saneEconomy.getTransactionLogger().logTransaction(new Transaction(
                        Economable.CONSOLE, economable, amount, TransactionReason.ADMIN_GIVE
                ));
            }

            return;
        }

        throw new InvalidUsageException();
    }
}
