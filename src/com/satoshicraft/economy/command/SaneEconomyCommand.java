package com.satoshicraft.economy.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.command.exception.CommandException;
import com.satoshicraft.economy.command.exception.type.usage.UsageException;
import com.satoshicraft.economy.exception.type.NoPermissionException;
import com.satoshicraft.economy.utils.MessageUtils;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public abstract class SaneEconomyCommand implements CommandExecutor {
    protected final SatoshiEconomy saneEconomy;

    public SaneEconomyCommand(SatoshiEconomy saneEconomy) {
        this.saneEconomy = saneEconomy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!sender.hasPermission(getPermission())) {
                throw new NoPermissionException();
            }

            onCommand(sender, args);
        } catch (UsageException e) {
            /* Invalid usage in some way, print out exactly what went wrong along with the proper usage. */
            MessageUtils.sendMessage(sender, e.getMessage());

            for (String s : getUsage()) {
                MessageUtils.sendMessage(sender, String.format("Usage: %s", s.replace("<command>", label)));
            }
        } catch (CommandException e) {
            MessageUtils.sendMessage(sender, e.getMessage());
        }

        return true;
    }

    /**
     * Get the permission node required to use the command.
     * @return Permission node.
     */
    public abstract String getPermission();

    /**
     * Get the command's usage.
     * When this is printed, '<command>' will be replaced with the command name.
     * @return Command usage examples
     */
    public abstract String[] getUsage();

    protected abstract void onCommand(CommandSender sender, String[] args) throws CommandException;
}
