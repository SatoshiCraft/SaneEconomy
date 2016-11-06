package com.satoshicraft.economy.command.type;

import org.bukkit.command.CommandSender;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.command.SatoshiEconomyCommand;
import com.satoshicraft.economy.command.exception.CommandException;
import com.satoshicraft.economy.command.exception.type.usage.InvalidUsageException;
import com.satoshicraft.economy.utils.MessageUtils;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class SaneEcoCommand extends SatoshiEconomyCommand {
    public SaneEcoCommand(SatoshiEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.admin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> reload-database"
        };
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new InvalidUsageException();
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("reload-database")) {
            MessageUtils.sendMessage(sender, "Reloading database...");
            saneEconomy.getEconomyManager().getBackend().reloadDatabase();
            MessageUtils.sendMessage(sender, "Database reloaded.");
        } else {
            throw new InvalidUsageException();
        }
    }
}
