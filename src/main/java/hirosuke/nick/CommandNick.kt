package hirosuke.nick

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class CommandNick : CommandExecutor, JavaPlugin() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && command.name == "nick") {
            var target = sender.player

            if (args[1].isNotEmpty()) {
                target = Bukkit.getPlayer(args[1])
            }

            if (args.size == 0) {
                setNick(target, target.name)
                sender.sendMessage("Nickname reset.")
            } else {
                if (args[0].length <= config.getInt("name_limit", 251)) {
                    setNick(target, args[0])
                    sender.sendMessage("Nickname set to §l" + args[0] + "§r.")
                }
            }
            return true
        } else if (command.name == "rlnick") {
            reloadConfig()
            sender.sendMessage("Nick Config reloaded.")
        }
        return false
    }

    fun setNick(player: Player, name: String) {
        var target = Bukkit.getPlayer(player.name)
        target.displayName = name
        target.playerListName = name
    }
}