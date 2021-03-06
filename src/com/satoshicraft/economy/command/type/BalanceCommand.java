package com.satoshicraft.economy.command.type;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.command.SatoshiEconomyCommand;
import com.satoshicraft.economy.command.exception.CommandException;
import com.satoshicraft.economy.command.exception.type.usage.NeedPlayerException;
import com.satoshicraft.economy.economy.economable.Economable;
import com.satoshicraft.economy.utils.MessageUtils;
import com.satoshicraft.economy.utils.PlayerUtils;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceCommand extends SatoshiEconomyCommand {
    public BalanceCommand(SatoshiEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.balance";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> [player]"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        String playerIdentifier;
        String playerName;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            Player player = (Player) sender;

            playerIdentifier = player.getPlayer().toString();
            playerName = player.getDisplayName();
        } else {
            playerIdentifier = args[0];
            playerName = args[0];

            if (!sender.hasPermission("saneeconomy.balance.other")) {
                MessageUtils.sendMessage(sender, "You don't have permission to check the balance of %s.", playerIdentifier);
                return;
            }
        }

        OfflinePlayer player = PlayerUtils.getOfflinePlayer(playerName);

        if (player == null) {
            MessageUtils.sendMessage(sender, "That player does not exist.");
            return;
        }

        MessageUtils.sendMessage(sender, "Balance for %s is %s.", playerName, saneEconomy.getEconomyManager().getFormattedBalance(Economable.wrap(player)));
    }
}
