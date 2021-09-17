package hirosuke.nick

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception

class CommandNick : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name == "nick") {

                var target = sender.player

                if (args.size >= 2) {
                    try {
                        target = Bukkit.getPlayer(args[1])
                    } catch (e: Exception) {
                        sender.sendMessage("Player is not online.")
                    }
                }

                if (args.isEmpty()) {
                    setNick(target, target.name)
                    NameTag.of(target.name).applyTo(target)
                    sender.sendMessage("Nickname reset.")
                } else {
                    if (args[0].length <= 16) {
                        var replaced = (args[0]+"&r").replace("&", "§")
                        setNick(target, replaced)
                        NameTag.of(replaced).applyTo(target)
                        sender.sendMessage("Nickname set to $replaced.")
                    } else {
                        sender.sendMessage("Name length must not exceed 16 characters.")
                    }
                }
                return true
            }
        }
        return true
    }

    fun setNick(player: Player, name: String) {
        var target = Bukkit.getPlayer(player.name)
        target.displayName = name
        target.playerListName = name
    }
}