package com.satoshicraft.economy.command.type;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.command.SatoshiEconomyCommand;
import com.satoshicraft.economy.command.exception.CommandException;
import com.satoshicraft.economy.command.exception.type.usage.TooManyArgumentsException;
import com.satoshicraft.economy.utils.MessageUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceTopCommand extends SatoshiEconomyCommand {
    public BalanceTopCommand(SatoshiEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.balancetop";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command>",
                "/<command> <page>"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            throw new TooManyArgumentsException();
        }

        int offset = 0;

        if (args.length == 1) {
            try {
                offset = 10 * Math.abs(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(sender, "%s is not a valid number.");
                return;
            }
        }

        Map<OfflinePlayer, Double> topBalances = saneEconomy.getEconomyManager().getTopPlayerBalances(10, offset);
        AtomicInteger index = new AtomicInteger(1); /* I know it's stupid, but you can't do some_int++ from within the lambda. */

        MessageUtils.sendMessage(sender, "Top %d players (page %s):", topBalances.size(), args[0]);
        topBalances.forEach((player, balance) -> MessageUtils.sendMessage(sender, "[%02d] %s - %s", index.getAndIncrement(), player.getName(), SatoshiEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(balance)));
    }
}
