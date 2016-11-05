package org.appledash.saneeconomy.utils;

import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static org.appledash.saneeconomy.utils.I18n._;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class MessageUtils {
    /**
     * Send a formatted chat message to the given target.
     * This message will have the prefix defined in SaneEconomy's config file.
     * @param target Target CommandSender
     * @param fmt String#format format
     * @param args String#format args
     */
    public static void sendMessage(CommandSender target, String fmt, Object... args) {
        fmt = _(fmt);
        String prefix = ChatColor.translateAlternateColorCodes('&', SaneEconomy.getInstance().getConfig().getString("chat.prefix", ""));
        target.sendMessage(prefix + String.format(fmt, (Object[])args));
    }
}
