package hirosuke.nick

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandNick : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && args.size >= 0 && command.isRegistered) {
            sender.displayName = args[0]
            sender.playerListName = args[0]
            sender.sendMessage("Nickname set to §l" + args[0] + "§r.")
            return true
        }
        return false
    }
}