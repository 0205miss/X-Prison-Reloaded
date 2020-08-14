package me.drawethree.wildprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

public class TokensHelpCommand extends TokensCommand {

    public TokensHelpCommand(WildPrisonTokens plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e&lTOKEN HELP MENU "));
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e/tokens pay [amount] [name]"));
            sender.sendMessage(Text.colorize("&e/tokens withdraw [amount] [value]"));
            sender.sendMessage(Text.colorize("&e/tokens [player]"));
            if (sender.hasPermission(WildPrisonTokens.TOKENS_ADMIN_PERM)) {
                sender.sendMessage(Text.colorize("&e/tokens give [amount] [player]"));
                sender.sendMessage(Text.colorize("&e/tokens remove [amount] [player]"));
                sender.sendMessage(Text.colorize("&e/tokens set [amount] [player]"));
            }
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            return true;
        }
        return false;
    }
}
